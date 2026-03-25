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
public class GroupMemberResponse {

    private Long userId;
    private String displayName;
    private String email;
    private String role;
    private String status;
    private LocalDateTime joinedAt;
}
