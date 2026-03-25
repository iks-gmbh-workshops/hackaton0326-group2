package com.hackaton.service;

import com.hackaton.dto.activity.ActivityResponse;
import com.hackaton.dto.admin.ChangeUserRoleRequest;
import com.hackaton.dto.admin.UserResponse;
import com.hackaton.dto.group.GroupResponse;
import com.hackaton.exception.BadRequestException;
import com.hackaton.exception.ResourceNotFoundException;
import com.hackaton.model.Activity;
import com.hackaton.model.Group;
import com.hackaton.model.User;
import com.hackaton.model.enums.MemberStatus;
import com.hackaton.model.enums.ParticipationStatus;
import com.hackaton.model.enums.UserRole;
import com.hackaton.repository.ActivityRepository;
import com.hackaton.repository.GroupRepository;
import com.hackaton.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ActivityRepository activityRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse changeUserRole(Long userId, ChangeUserRoleRequest request) {
        User user = findUser(userId);

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be USER or ADMIN");
        }

        user.setRole(newRole);
        userRepository.save(user);
        return toUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findUser(userId);
        userRepository.delete(user);
    }

    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::toGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        groupRepository.delete(group);
    }

    public List<ActivityResponse> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(this::toActivityResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteActivity(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
        activityRepository.delete(activity);
    }

    // --- Hilfsmethoden ---

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .agbAccepted(user.isAgbAccepted())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private GroupResponse toGroupResponse(Group group) {
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

    private ActivityResponse toActivityResponse(Activity activity) {
        List<ActivityResponse.GroupInfo> groups = activity.getGroupActivities().stream()
                .map(ga -> ActivityResponse.GroupInfo.builder()
                        .id(ga.getGroup().getId())
                        .name(ga.getGroup().getName())
                        .build())
                .collect(Collectors.toList());

        int participantCount = (int) activity.getParticipants().stream()
                .filter(p -> p.getStatus() == ParticipationStatus.ACCEPTED)
                .count();

        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .location(activity.getLocation())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .createdById(activity.getCreatedBy().getId())
                .createdByName(activity.getCreatedBy().getDisplayName())
                .createdAt(activity.getCreatedAt())
                .groups(groups)
                .participantCount(participantCount)
                .build();
    }
}
