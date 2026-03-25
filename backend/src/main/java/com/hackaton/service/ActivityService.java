package com.hackaton.service;

import com.hackaton.dto.activity.ActivityResponse;
import com.hackaton.dto.activity.CreateActivityRequest;
import com.hackaton.dto.activity.UpdateActivityRequest;
import com.hackaton.model.*;
import com.hackaton.model.enums.GroupRole;
import com.hackaton.model.enums.MemberStatus;
import com.hackaton.model.enums.UserRole;
import com.hackaton.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final GroupRepository groupRepository;
    private final GroupActivityRepository groupActivityRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ActivityResponse createActivity(Long userId, CreateActivityRequest request) {
        User user = findUser(userId);

        // Prüfen ob User Verwalter in mindestens einer der angegebenen Gruppen ist
        for (Long groupId : request.getGroupIds()) {
            checkVerwalterOrAdmin(userId, groupId, user.getRole());
        }

        Activity activity = Activity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createdBy(user)
                .build();

        activityRepository.save(activity);

        // Gruppen zuordnen
        for (Long groupId : request.getGroupIds()) {
            Group group = findGroup(groupId);
            GroupActivity ga = GroupActivity.builder()
                    .group(group)
                    .activity(activity)
                    .build();
            groupActivityRepository.save(ga);
        }

        return toResponse(activity);
    }

    public ActivityResponse getActivity(Long activityId, Long userId) {
        Activity activity = findActivity(activityId);
        checkActivityAccess(activity, userId);
        return toResponse(activity);
    }

    @Transactional
    public ActivityResponse updateActivity(Long activityId, Long userId, UserRole userRole, UpdateActivityRequest request) {
        Activity activity = findActivity(activityId);
        checkActivityEditPermission(activity, userId, userRole);

        if (StringUtils.hasText(request.getTitle())) {
            activity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            activity.setLocation(request.getLocation());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }

        activityRepository.save(activity);
        return toResponse(activity);
    }

    @Transactional
    public void deleteActivity(Long activityId, Long userId, UserRole userRole) {
        Activity activity = findActivity(activityId);
        checkActivityEditPermission(activity, userId, userRole);
        activityRepository.delete(activity);
    }

    public List<ActivityResponse> getGroupActivities(Long groupId, Long userId) {
        checkMemberOrAdmin(userId, groupId);

        List<GroupActivity> groupActivities = groupActivityRepository.findByGroupId(groupId);
        return groupActivities.stream()
                .map(ga -> toResponse(ga.getActivity()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityResponse addGroupsToActivity(Long activityId, Long userId, UserRole userRole, List<Long> groupIds) {
        Activity activity = findActivity(activityId);
        checkActivityEditPermission(activity, userId, userRole);

        for (Long groupId : groupIds) {
            if (groupActivityRepository.existsByGroupIdAndActivityId(groupId, activityId)) {
                continue;
            }
            checkVerwalterOrAdmin(userId, groupId, userRole);
            Group group = findGroup(groupId);
            GroupActivity ga = GroupActivity.builder()
                    .group(group)
                    .activity(activity)
                    .build();
            groupActivityRepository.save(ga);
        }

        // Refresh um neue GroupActivities zu laden
        return toResponse(findActivity(activityId));
    }

    @Transactional
    public void removeGroupFromActivity(Long activityId, Long groupId, Long userId, UserRole userRole) {
        Activity activity = findActivity(activityId);
        checkActivityEditPermission(activity, userId, userRole);

        if (!groupActivityRepository.existsByGroupIdAndActivityId(groupId, activityId)) {
            throw new IllegalArgumentException("Group is not assigned to this activity");
        }

        long assignedGroupCount = groupActivityRepository.findByActivityId(activityId).size();
        if (assignedGroupCount <= 1) {
            throw new IllegalArgumentException("Cannot remove the last group from an activity");
        }

        groupActivityRepository.deleteByGroupIdAndActivityId(groupId, activityId);
    }

    // --- Hilfsmethoden ---

    private void checkActivityAccess(Activity activity, Long userId) {
        User user = findUser(userId);
        if (user.getRole() == UserRole.ADMIN) return;

        // User muss Mitglied in mindestens einer zugeordneten Gruppe sein
        boolean hasAccess = activity.getGroupActivities().stream()
                .anyMatch(ga -> groupMemberRepository.findByUserIdAndGroupId(userId, ga.getGroup().getId())
                        .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                        .isPresent());

        if (!hasAccess) {
            throw new IllegalArgumentException("No access to this activity");
        }
    }

    private void checkActivityEditPermission(Activity activity, Long userId, UserRole userRole) {
        if (userRole == UserRole.ADMIN) return;

        // Ersteller oder Verwalter in einer zugeordneten Gruppe
        if (activity.getCreatedBy().getId().equals(userId)) return;

        boolean isVerwalter = activity.getGroupActivities().stream()
                .anyMatch(ga -> {
                    var member = groupMemberRepository.findByUserIdAndGroupId(userId, ga.getGroup().getId());
                    return member.filter(gm -> gm.getStatus() == MemberStatus.ACTIVE && gm.getRole() == GroupRole.VERWALTER).isPresent();
                });

        if (!isVerwalter) {
            throw new IllegalArgumentException("Only the creator, a Verwalter, or an Admin can edit this activity");
        }
    }

    private void checkMemberOrAdmin(Long userId, Long groupId) {
        User user = findUser(userId);
        if (user.getRole() == UserRole.ADMIN) return;

        groupMemberRepository.findByUserIdAndGroupId(userId, groupId)
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Not a member of this group"));
    }

    private void checkVerwalterOrAdmin(Long userId, Long groupId, UserRole userRole) {
        if (userRole == UserRole.ADMIN) return;

        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(userId, groupId)
                .filter(gm -> gm.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Not a member of group " + groupId));

        if (member.getRole() != GroupRole.VERWALTER) {
            throw new IllegalArgumentException("Only Verwalter or Admin can create activities for group " + groupId);
        }
    }

    private Activity findActivity(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private ActivityResponse toResponse(Activity activity) {
        List<ActivityResponse.GroupInfo> groups = activity.getGroupActivities().stream()
                .map(ga -> ActivityResponse.GroupInfo.builder()
                        .id(ga.getGroup().getId())
                        .name(ga.getGroup().getName())
                        .build())
                .collect(Collectors.toList());

        int participantCount = (int) activity.getParticipants().stream()
                .filter(p -> p.getStatus() == com.hackaton.model.enums.ParticipationStatus.ACCEPTED)
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
