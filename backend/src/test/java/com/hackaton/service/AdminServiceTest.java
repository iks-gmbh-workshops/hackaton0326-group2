package com.hackaton.service;

import com.hackaton.dto.activity.ActivityResponse;
import com.hackaton.dto.admin.ChangeUserRoleRequest;
import com.hackaton.dto.admin.UserResponse;
import com.hackaton.dto.group.GroupResponse;
import com.hackaton.exception.BadRequestException;
import com.hackaton.exception.ResourceNotFoundException;
import com.hackaton.model.*;
import com.hackaton.model.enums.MemberStatus;
import com.hackaton.model.enums.ParticipationStatus;
import com.hackaton.model.enums.UserRole;
import com.hackaton.repository.ActivityRepository;
import com.hackaton.repository.GroupRepository;
import com.hackaton.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private ActivityRepository activityRepository;

    @InjectMocks private AdminService adminService;

    private User testUser;
    private Group testGroup;
    private Activity testActivity;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).email("user@test.com").displayName("User")
                .role(UserRole.USER).agbAccepted(true)
                .createdAt(LocalDateTime.now()).build();

        testGroup = Group.builder()
                .id(10L).name("Group").description("Desc")
                .createdBy(testUser).inviteToken("token")
                .members(new ArrayList<>()).build();

        testActivity = Activity.builder()
                .id(100L).title("Activity").description("Desc").location("Here")
                .startTime(LocalDateTime.now().plusDays(1))
                .createdBy(testUser)
                .groupActivities(new ArrayList<>())
                .participants(new ArrayList<>())
                .build();
    }

    @Nested
    class GetAllUsers {
        @Test
        void returnsList() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<UserResponse> result = adminService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("user@test.com");
            assertThat(result.get(0).getRole()).isEqualTo("USER");
        }

        @Test
        void emptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponse> result = adminService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ChangeUserRole {
        @Test
        void toAdmin_success() {
            ChangeUserRoleRequest request = ChangeUserRoleRequest.builder().role("ADMIN").build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserResponse response = adminService.changeUserRole(1L, request);

            assertThat(testUser.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(response.getRole()).isEqualTo("ADMIN");
            verify(userRepository).save(testUser);
        }

        @Test
        void invalidRole_throwsBadRequest() {
            ChangeUserRoleRequest request = ChangeUserRoleRequest.builder().role("SUPERADMIN").build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> adminService.changeUserRole(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid role");
        }

        @Test
        void userNotFound() {
            ChangeUserRoleRequest request = ChangeUserRoleRequest.builder().role("ADMIN").build();
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.changeUserRole(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteUser {
        @Test
        void success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            adminService.deleteUser(1L);

            verify(userRepository).delete(testUser);
        }

        @Test
        void notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.deleteUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetAllGroups {
        @Test
        void returnsList() {
            // Add an active member for memberCount
            GroupMember gm = GroupMember.builder()
                    .id(200L).user(testUser).group(testGroup)
                    .status(MemberStatus.ACTIVE).build();
            testGroup.getMembers().add(gm);

            when(groupRepository.findAll()).thenReturn(List.of(testGroup));

            List<GroupResponse> result = adminService.getAllGroups();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Group");
            assertThat(result.get(0).getMemberCount()).isEqualTo(1);
        }
    }

    @Nested
    class DeleteGroup {
        @Test
        void success() {
            when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));

            adminService.deleteGroup(10L);

            verify(groupRepository).delete(testGroup);
        }

        @Test
        void notFound() {
            when(groupRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.deleteGroup(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetAllActivities {
        @Test
        void returnsList() {
            when(activityRepository.findAll()).thenReturn(List.of(testActivity));

            List<ActivityResponse> result = adminService.getAllActivities();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Activity");
            assertThat(result.get(0).getParticipantCount()).isEqualTo(0);
        }

        @Test
        void withParticipants() {
            ActivityParticipant accepted = ActivityParticipant.builder()
                    .id(300L).activity(testActivity).user(testUser)
                    .status(ParticipationStatus.ACCEPTED).build();
            testActivity.getParticipants().add(accepted);

            when(activityRepository.findAll()).thenReturn(List.of(testActivity));

            List<ActivityResponse> result = adminService.getAllActivities();

            assertThat(result.get(0).getParticipantCount()).isEqualTo(1);
        }
    }

    @Nested
    class DeleteActivity {
        @Test
        void success() {
            when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));

            adminService.deleteActivity(100L);

            verify(activityRepository).delete(testActivity);
        }

        @Test
        void notFound() {
            when(activityRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.deleteActivity(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
