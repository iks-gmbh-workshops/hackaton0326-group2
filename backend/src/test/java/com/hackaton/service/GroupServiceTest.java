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
class GroupServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks private GroupService groupService;

    private User user;
    private User otherUser;
    private Group group;
    private GroupMember activeMember;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("user@test.com").displayName("User")
                .role(UserRole.USER).agbAccepted(true).build();

        otherUser = User.builder()
                .id(2L).email("other@test.com").displayName("Other")
                .role(UserRole.USER).agbAccepted(true).build();

        group = Group.builder()
                .id(10L).name("Test Group").description("Desc")
                .createdBy(user).inviteToken("token-123")
                .members(new ArrayList<>()).build();

        activeMember = GroupMember.builder()
                .id(100L).user(user).group(group)
                .role(GroupRole.VERWALTER).status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now()).build();

        group.getMembers().add(activeMember);
    }

    @Nested
    class CreateGroup {
        @Test
        void success() {
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .name("New Group").description("Desc").build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupRepository.save(any(Group.class))).thenAnswer(inv -> {
                Group g = inv.getArgument(0);
                g.setId(10L);
                g.setMembers(new ArrayList<>());
                return g;
            });
            when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

            GroupResponse response = groupService.createGroup(1L, request);

            assertThat(response.getName()).isEqualTo("New Group");
            verify(groupRepository).save(any(Group.class));
            verify(groupMemberRepository).save(any(GroupMember.class));
        }

        @Test
        void userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupService.createGroup(99L, CreateGroupRequest.builder().name("G").build()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetMyGroups {
        @Test
        void returnsActiveGroups() {
            when(groupMemberRepository.findByUserIdAndStatus(1L, MemberStatus.ACTIVE))
                    .thenReturn(List.of(activeMember));

            List<GroupResponse> result = groupService.getMyGroups(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Group");
        }
    }

    @Nested
    class GetGroup {
        @Test
        void success_asMember() {
            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(activeMember));

            GroupResponse response = groupService.getGroup(10L, 1L);

            assertThat(response.getName()).isEqualTo("Test Group");
        }

        @Test
        void notFound() {
            when(groupRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupService.getGroup(99L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void forbidden_notMember() {
            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupService.getGroup(10L, 2L))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    class DeleteGroup {
        @Test
        void success_asVerwalter() {
            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L))
                    .thenReturn(Optional.of(activeMember));

            groupService.deleteGroup(10L, 1L, UserRole.USER);

            verify(groupRepository).delete(group);
        }

        @Test
        void success_asAdmin() {
            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

            groupService.deleteGroup(10L, 99L, UserRole.ADMIN);

            verify(groupRepository).delete(group);
        }
    }

    @Nested
    class JoinByToken {
        @Test
        void success_newMember() {
            when(groupRepository.findByInviteToken("token-123")).thenReturn(Optional.of(group));
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.empty());
            when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

            GroupResponse response = groupService.joinByToken("token-123", 2L);

            assertThat(response.getName()).isEqualTo("Test Group");
            verify(groupMemberRepository).save(any(GroupMember.class));
        }

        @Test
        void alreadyActive_throwsConflict() {
            GroupMember existing = GroupMember.builder()
                    .id(101L).user(otherUser).group(group)
                    .role(GroupRole.MITGLIED).status(MemberStatus.ACTIVE).build();

            when(groupRepository.findByInviteToken("token-123")).thenReturn(Optional.of(group));
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> groupService.joinByToken("token-123", 2L))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        void pendingMember_getsActivated() {
            GroupMember pending = GroupMember.builder()
                    .id(101L).user(otherUser).group(group)
                    .role(GroupRole.MITGLIED).status(MemberStatus.PENDING).build();

            when(groupRepository.findByInviteToken("token-123")).thenReturn(Optional.of(group));
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.of(pending));
            when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

            groupService.joinByToken("token-123", 2L);

            assertThat(pending.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            verify(groupMemberRepository).save(pending);
        }

        @Test
        void invalidToken_throwsNotFound() {
            when(groupRepository.findByInviteToken("bad-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupService.joinByToken("bad-token", 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class AcceptInvitation {
        @Test
        void success() {
            GroupMember pending = GroupMember.builder()
                    .id(200L).user(user).group(group)
                    .status(MemberStatus.PENDING).build();

            when(groupMemberRepository.findById(200L)).thenReturn(Optional.of(pending));
            when(groupMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            groupService.acceptInvitation(200L, 1L);

            assertThat(pending.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(pending.getJoinedAt()).isNotNull();
        }

        @Test
        void notYourInvitation_throwsForbidden() {
            GroupMember pending = GroupMember.builder()
                    .id(200L).user(otherUser).group(group)
                    .status(MemberStatus.PENDING).build();

            when(groupMemberRepository.findById(200L)).thenReturn(Optional.of(pending));

            assertThatThrownBy(() -> groupService.acceptInvitation(200L, 1L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void alreadyActive_throwsBadRequest() {
            GroupMember active = GroupMember.builder()
                    .id(200L).user(user).group(group)
                    .status(MemberStatus.ACTIVE).build();

            when(groupMemberRepository.findById(200L)).thenReturn(Optional.of(active));

            assertThatThrownBy(() -> groupService.acceptInvitation(200L, 1L))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    class DeclineInvitation {
        @Test
        void success() {
            GroupMember pending = GroupMember.builder()
                    .id(200L).user(user).group(group)
                    .status(MemberStatus.PENDING).build();

            when(groupMemberRepository.findById(200L)).thenReturn(Optional.of(pending));
            when(groupMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            groupService.declineInvitation(200L, 1L);

            assertThat(pending.getStatus()).isEqualTo(MemberStatus.LEFT);
        }
    }

    @Nested
    class LeaveGroup {
        @Test
        void success_asMitglied() {
            GroupMember mitglied = GroupMember.builder()
                    .id(101L).user(otherUser).group(group)
                    .role(GroupRole.MITGLIED).status(MemberStatus.ACTIVE).build();

            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.of(mitglied));
            when(groupMemberRepository.findByGroupId(10L)).thenReturn(List.of(activeMember, mitglied));
            when(groupMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            groupService.leaveGroup(10L, 2L);

            assertThat(mitglied.getStatus()).isEqualTo(MemberStatus.LEFT);
        }

        @Test
        void lastVerwalter_throwsBadRequest() {
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L)).thenReturn(Optional.of(activeMember));
            when(groupMemberRepository.findByGroupId(10L)).thenReturn(List.of(activeMember));

            assertThatThrownBy(() -> groupService.leaveGroup(10L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("last Verwalter");
        }
    }

    @Nested
    class RemoveMember {
        @Test
        void success() {
            GroupMember target = GroupMember.builder()
                    .id(101L).user(otherUser).group(group)
                    .role(GroupRole.MITGLIED).status(MemberStatus.ACTIVE).build();

            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L)).thenReturn(Optional.of(activeMember));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.of(target));
            when(groupMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            groupService.removeMember(10L, 2L, 1L, UserRole.USER);

            assertThat(target.getStatus()).isEqualTo(MemberStatus.LEFT);
        }

        @Test
        void removeVerwalter_asNonAdmin_throwsForbidden() {
            GroupMember targetVerwalter = GroupMember.builder()
                    .id(101L).user(otherUser).group(group)
                    .role(GroupRole.VERWALTER).status(MemberStatus.ACTIVE).build();

            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L)).thenReturn(Optional.of(activeMember));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.of(targetVerwalter));

            assertThatThrownBy(() -> groupService.removeMember(10L, 2L, 1L, UserRole.USER))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Verwalter");
        }
    }

    @Nested
    class ChangeMemberRole {
        @Test
        void success() {
            GroupMember target = GroupMember.builder()
                    .id(101L).user(otherUser).group(group)
                    .role(GroupRole.MITGLIED).status(MemberStatus.ACTIVE).build();

            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L)).thenReturn(Optional.of(activeMember));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.of(target));
            when(groupMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoleRequest request = ChangeRoleRequest.builder().role("VERWALTER").build();
            GroupMemberResponse response = groupService.changeMemberRole(10L, 2L, 1L, UserRole.USER, request);

            assertThat(target.getRole()).isEqualTo(GroupRole.VERWALTER);
            assertThat(response.getRole()).isEqualTo("VERWALTER");
        }

        @Test
        void invalidRole_throwsBadRequest() {
            GroupMember target = GroupMember.builder()
                    .id(101L).user(otherUser).group(group)
                    .role(GroupRole.MITGLIED).status(MemberStatus.ACTIVE).build();

            when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
            when(groupMemberRepository.findByUserIdAndGroupId(1L, 10L)).thenReturn(Optional.of(activeMember));
            when(groupMemberRepository.findByUserIdAndGroupId(2L, 10L)).thenReturn(Optional.of(target));

            ChangeRoleRequest request = ChangeRoleRequest.builder().role("INVALID").build();

            assertThatThrownBy(() -> groupService.changeMemberRole(10L, 2L, 1L, UserRole.USER, request))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}
