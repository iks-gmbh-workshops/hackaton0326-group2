package com.hackaton.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInvitationResponse {

    private Long id;
    private Long groupId;
    private String groupName;
    private String description;
    private int memberCount;
    private String createdByName;
    private String invitedByName;
    private LocalDateTime invitedAt;
    private String status;
}
