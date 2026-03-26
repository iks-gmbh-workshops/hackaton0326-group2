package com.hackaton.model;

import com.hackaton.model.enums.GroupRole;
import com.hackaton.model.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "group_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupRole role = GroupRole.MITGLIED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.PENDING;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;
}
