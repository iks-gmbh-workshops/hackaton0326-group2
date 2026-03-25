package com.hackaton.dto.activity;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddGroupsRequest {

    @NotEmpty(message = "At least one group ID must be specified")
    private List<Long> groupIds;
}
