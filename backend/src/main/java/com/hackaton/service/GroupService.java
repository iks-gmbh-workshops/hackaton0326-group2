package com.hackaton.service;

import com.hackaton.dto.group.*;
import com.hackaton.exception.BadRequestException;
import com.hackaton.exception.ConflictException;
import com.hackaton.exception.ForbiddenException;
import com.hackaton.exception.ResourceNotFoundException;
import com.hackaton.model.Group;
import com.hackaton.model.GroupMember;
import com.hackaton.model.User;
import com.hackaton.model.enums.GroupRole;
import com.hackaton.model.enums.MemberStatus;
import com.hackaton.model.enums.UserRole;
import com.hackaton.repository.GroupMemberRepository;
import com.hackaton.repository.GroupRepository;
import com.hackaton.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public GroupResponse createGroup(Long userId, CreateGroupRequest request) {
        User user = findUser(userId);

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(user)
                .inviteToken(UUID.randomUUID().toString())
                .build();

        groupRepository.save(group);

        GroupMember member = GroupMember.builder()
                .user(user)
                .group(group)
                .role(GroupRole.VERWALTER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        groupMemberRepository.save(member);

        return toResponse(group);
    }

    public List<GroupResponse> getMyGroups(Long userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE);
        return memberships.stream()
                .map(gm -> toResponse(gm.getGroup()))
                .collect(Collectors.toList());
    }

    public GroupResponse getGroup(Long groupId, Long userId) {
        Group group = findGroup(groupId);
        checkMemberOrAdmin(userId, groupId);
        return toResponse(group);
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, Long userId, UserRole userRole, UpdateGroupRequest request) {
        Group group = findGroup(groupId);
        checkVerwalterOrAdmin(userId, groupId, userRole);

        if (StringUtils.hasText(request.getName())) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long userId, UserRole userRole) {
        Group group = findGroup(groupId);
        checkVerwalterOrAdmin(userId, groupId, userRole);
        groupRepository.delete(group);
    }

    public List<GroupMemberResponse> getMembers(Long groupId, Long userId) {
        checkMemberOrAdmin(userId, groupId);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void inviteMember(Long groupId, Long userId, UserRole userRole, InviteRequest request) {
        Group group = findGroup(groupId);
        checkVerwalterOrAdmin(userId, groupId, userRole);
        User inviter = findUser(userId);

        // Prüfe ob eingeladener User existiert
        userRepository.findByEmail(request.getEmail()).ifPresent(invitedUser -> {
            // Prüfe ob bereits Mitglied oder eingeladen
            var existing = groupMemberRepository.findByUserIdAndGroupId(invitedUser.getId(), groupId);
            if (existing.isPresent()) {
                MemberStatus st = existing.get().getStatus();
                if (st == MemberStatus.ACTIVE) {
                    throw new ConflictException("User is already a member of this group");
                }
                if (st == MemberStatus.PENDING) {
                    throw new ConflictException("User is already invited to this group");
                }
                // Status LEFT → erneut einladen
                existing.get().setStatus(MemberStatus.PENDING);
                existing.get().setInvitedBy(inviter);
                groupMemberRepository.save(existing.get());
                return;
            }

            GroupMember member = GroupMember.builder()
                    .user(invitedUser)
                    .group(group)
                    .role(GroupRole.MITGLIED)
                    .status(MemberStatus.PENDING)
                    .invitedBy(inviter)
                    .build();
            groupMemberRepository.save(member);
        });

        emailService.sendGroupInvitation(request.getEmail(), group.getName(), group.getInviteToken());
    }

    public List<GroupInvitationResponse> getMyInvitations(Long userId) {
        List<GroupMember> pending = groupMemberRepository.findByUserIdAndStatus(userId, MemberStatus.PENDING);
        return pending.stream()
                .map(this::toInvitationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptInvitation(Long membershipId, Long userId) {
        GroupMember member = groupMemberRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!member.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Not your invitation");
        }
        if (member.getStatus() != MemberStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }

        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        groupMemberRepository.save(member);
    }

    @Transactional
    public void declineInvitation(Long membershipId, Long userId) {
        GroupMember member = groupMemberRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!member.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Not your invitation");
        }
        if (member.getStatus() != MemberStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }

        member.setStatus(MemberStatus.LEFT);
        groupMemberRepository.save(member);
    }

    @Transactional
    public GroupResponse joinByToken(String inviteToken, Long userId) {
        Group group = groupRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite token"));

        User user = findUser(userId);

        var existing = groupMemberRepository.findByUserIdAndGroupId(userId, group.getId());
        if (existing.isPresent()) {
            GroupMember gm = existing.get();
            if (gm.getStatus() == MemberStatus.ACTIVE) {
                throw new ConflictException("User is already a member of this group");
            }
            // PENDING oder LEFT → aktivieren
            gm.setStatus(MemberStatus.ACTIVE);
            gm.setJoinedAt(LocalDateTime.now());
            groupMemberRepository.save(gm);
            return toResponse(group);
        }

        GroupMember member = GroupMember.builder()
                .user(user)
                .group(group)
                .role(GroupRole.MITGLIED)
                .status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        groupMemberRepository.save(member);

        return toResponse(group);
    }

    @Transactional
    public void removeMember(Long groupId, Long targetUserId, Long requesterId, UserRole requesterRole) {
        findGroup(groupId);
        checkVerwalterOrAdmin(requesterId, groupId, requesterRole);

        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(targetUserId, groupId)
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User is not an active member of this group"));

        if (member.getRole() == GroupRole.VERWALTER && requesterRole != UserRole.ADMIN) {
            throw new ForbiddenException("Cannot remove a Verwalter unless you are an Admin");
        }

        member.setStatus(MemberStatus.LEFT);
        groupMemberRepository.save(member);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(userId, groupId)
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Not a member of this group"));

        long activeVerwalterCount = groupMemberRepository.findByGroupId(groupId).stream()
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE && gm.getRole() == GroupRole.VERWALTER)
                .count();

        if (member.getRole() == GroupRole.VERWALTER && activeVerwalterCount <= 1) {
            throw new BadRequestException("Cannot leave group as the last Verwalter. Transfer the role first.");
        }

        member.setStatus(MemberStatus.LEFT);
        groupMemberRepository.save(member);
    }

    @Transactional
    public GroupMemberResponse changeMemberRole(Long groupId, Long targetUserId, Long requesterId, UserRole requesterRole, ChangeRoleRequest request) {
        findGroup(groupId);
        checkVerwalterOrAdmin(requesterId, groupId, requesterRole);

        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(targetUserId, groupId)
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User is not an active member of this group"));

        GroupRole newRole;
        try {
            newRole = GroupRole.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be VERWALTER or MITGLIED");
        }

        member.setRole(newRole);
        groupMemberRepository.save(member);

        return toMemberResponse(member);
    }

    // --- Hilfsmethoden ---

    private void checkMemberOrAdmin(Long userId, Long groupId) {
        User user = findUser(userId);
        if (user.getRole() == UserRole.ADMIN) return;

        groupMemberRepository.findByUserIdAndGroupId(userId, groupId)
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));
    }

    private void checkVerwalterOrAdmin(Long userId, Long groupId, UserRole userRole) {
        if (userRole == UserRole.ADMIN) return;

        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(userId, groupId)
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));

        if (member.getRole() != GroupRole.VERWALTER) {
            throw new ForbiddenException("Only Verwalter or Admin can perform this action");
        }
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private GroupResponse toResponse(Group group) {
        int memberCount = (int) group.getMembers().stream()
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .count();

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdById(group.getCreatedBy().getId())
                .createdByName(group.getCreatedBy().getDisplayName())
                .createdAt(group.getCreatedAt())
                .inviteToken(group.getInviteToken())
                .memberCount(memberCount)
                .build();
    }

    private GroupMemberResponse toMemberResponse(GroupMember gm) {
        return GroupMemberResponse.builder()
                .userId(gm.getUser().getId())
                .displayName(gm.getUser().getDisplayName())
                .email(gm.getUser().getEmail())
                .role(gm.getRole().name())
                .status(gm.getStatus().name())
                .joinedAt(gm.getJoinedAt())
                .build();
    }

    private GroupInvitationResponse toInvitationResponse(GroupMember gm) {
        Group group = gm.getGroup();
        int memberCount = (int) group.getMembers().stream()
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .count();

        return GroupInvitationResponse.builder()
                .id(gm.getId())
                .groupId(group.getId())
                .groupName(group.getName())
                .description(group.getDescription())
                .memberCount(memberCount)
                .createdByName(group.getCreatedBy().getDisplayName())
                .invitedByName(gm.getInvitedBy() != null ? gm.getInvitedBy().getDisplayName() : null)
                .invitedAt(gm.getJoinedAt())
                .status(gm.getStatus().name())
                .build();
    }
}
