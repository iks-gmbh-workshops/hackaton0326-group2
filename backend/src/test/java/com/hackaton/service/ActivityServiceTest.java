package com.hackaton.service;

import com.hackaton.dto.activity.*;
import com.hackaton.exception.BadRequestException;
import com.hackaton.exception.ForbiddenException;
import com.hackaton.exception.ResourceNotFoundException;
import com.hackaton.model.*;
import com.hackaton.model.enums.GroupRole;
import com.hackaton.model.enums.MemberStatus;
import com.hackaton.model.enums.ParticipationStatus;
import com.hackaton.model.enums.UserRole;
import com.hackaton.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private ActivityParticipantRepository activityParticipantRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupActivityRepository groupActivityRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ActivityService activityService;

    private User user;
    private User adminUser;
    private Group group;
    private Activity activity;
    private GroupActivity groupActivity;
    private GroupMember verwalterMember;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("user@test.com").displayName("User")
                .role(UserRole.USER).agbAccepted(true).build();

        adminUser = User.builder()
                .id(99L).email("admin@test.com").displayName("Admin")
                .role(UserRole.ADMIN).agbAccepted(true).build();

        group = Group.builder()
                .id(10L).name("Group").description("Desc")
                .createdBy(user).inviteToken("token")
                .members(new ArrayList<>()).groupActivities(new ArrayList<>()).build();

        activity = Activity.builder()
                .id(100L).title("Activity").description("Desc").location("Here")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .createdBy(user)
                .groupActivities(new ArrayList<>())
                .participants(new ArrayList<>())
                .build();

        groupActivity = GroupActivity.builder()
                .id(1000L).group(group).activity(activity).build();
        activity.getGroupActivities().add(groupActivity);

        verwalterMember = GroupMember.builder()
                .id(200L).user(user).group(group)
                .role(GroupRole.VERWALTER).status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now()).build();
        group.getMembers().add(verwalterMember);
    }

    @Nested
    class CreateActivity {
        @Test
        void success() {
            CreateActivityRequest request = CreateActivityRequest.builder()
                    .title("New Activity").description("D").location("L")
                    .startTime(LocalDateTime.now().plusDays(1))
                    .groupIds(List.of(10L)).build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(verwalterMember));
            when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> {
                Activity a = inv.getArgument(0);
                a.setId(100L);
                return a;
            });
            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(groupActivityRepository.save(any(GroupActivity.class))).thenAnswer(inv -> inv.getArgument(0));

            ActivityResponse response = activityService.createActivity(1L, request);

            assertThat(response.getTitle()).isEqualTo("New Activity");
            verify(activityRepository).save(any(Activity.class));
            verify(groupActivityRepository).save(any(GroupActivity.class));
        }

        @Test
        void forbidden_notVerwalter() {
            GroupMember mitglied = GroupMember.builder()
                    .id(201L).user(user).group(group)
                    .role(GroupRole.MITGLIED).status(MemberStatus.ACTIVE).build();

            CreateActivityRequest request = CreateActivityRequest.builder()
                    .title("Act").startTime(LocalDateTime.now().plusDays(1))
                    .groupIds(List.of(10L)).build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(mitglied));

            assertThatThrownBy(() -> activityService.createActivity(1L, request))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    class GetActivity {
        @Test
        void success_asMember() {
            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(verwalterMember));

            ActivityResponse response = activityService.getActivity(100L, 1L);

            assertThat(response.getTitle()).isEqualTo("Activity");
        }

        @Test
        void notFound() {
            when(activityRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> activityService.getActivity(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void forbidden_notMember() {
            User stranger = User.builder()
                    .id(50L).email("s@t.com").displayName("S")
                    .role(UserRole.USER).build();

            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(userRepository.findById(50L)).thenReturn(Optional.of(stranger));
            when(groupMemberRepository.findByUserIdAndGroupId(50L, 10L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> activityService.getActivity(100L, 50L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void success_asAdmin() {
            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));

            ActivityResponse response = activityService.getActivity(100L, 99L);

            assertThat(response.getTitle()).isEqualTo("Activity");
        }
    }

    @Nested
    class DeleteActivity {
        @Test
        void success_asCreator() {
            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));

            activityService.deleteActivity(100L, 1L, UserRole.USER);

            verify(activityRepository).delete(activity);
        }

        @Test
        void success_asAdmin() {
            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));

            activityService.deleteActivity(100L, 99L, UserRole.ADMIN);

            verify(activityRepository).delete(activity);
        }
    }

    @Nested
    class RespondToActivity {
        @Test
        void accept_success() {
            RespondRequest request = RespondRequest.builder().status("ACCEPTED").build();

            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(verwalterMember));
            when(activityParticipantRepository.findByActivityIdAndUserId(100L, 1L))
                    .thenReturn(Optional.empty());
            when(activityParticipantRepository.save(any(ActivityParticipant.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ParticipantResponse response = activityService.respondToActivity(100L, 1L, request);

            assertThat(response.getStatus()).isEqualTo("ACCEPTED");
            assertThat(response.getDisplayName()).isEqualTo("User");
        }

        @Test
        void invalidStatus_throwsBadRequest() {
            RespondRequest request = RespondRequest.builder().status("INVALID").build();

            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(verwalterMember));

            assertThatThrownBy(() -> activityService.respondToActivity(100L, 1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid status");
        }

        @Test
        void pendingStatus_throwsBadRequest() {
            RespondRequest request = RespondRequest.builder().status("PENDING").build();

            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(verwalterMember));

            assertThatThrownBy(() -> activityService.respondToActivity(100L, 1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PENDING");
        }
    }

    @Nested
    class RemoveGroupFromActivity {
        @Test
        void success() {
            GroupActivity ga2 = GroupActivity.builder().id(1001L).group(group).activity(activity).build();

            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(groupActivityRepository.existsByGroupIdAndActivityId(10L, 100L)).thenReturn(true);
            when(groupActivityRepository.findByActivityId(100L)).thenReturn(List.of(groupActivity, ga2));

            activityService.removeGroupFromActivity(100L, 10L, 1L, UserRole.USER);

            verify(groupActivityRepository).deleteByGroupIdAndActivityId(10L, 100L);
        }

        @Test
        void lastGroup_throwsBadRequest() {
            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(groupActivityRepository.existsByGroupIdAndActivityId(10L, 100L)).thenReturn(true);
            when(groupActivityRepository.findByActivityId(100L)).thenReturn(List.of(groupActivity));

            assertThatThrownBy(() -> activityService.removeGroupFromActivity(100L, 10L, 1L, UserRole.USER))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("last group");
        }

        @Test
        void groupNotAssigned_throwsNotFound() {
            when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
            when(groupActivityRepository.existsByGroupIdAndActivityId(20L, 100L)).thenReturn(false);

            assertThatThrownBy(() -> activityService.removeGroupFromActivity(100L, 20L, 1L, UserRole.USER))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetUpcomingActivities {
        @Test
        void returnsActivities() {
            when(activityRepository.findUpcomingByUserId(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(List.of(activity));

            List<ActivityResponse> result = activityService.getUpcomingActivities(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Activity");
        }
    }
}
