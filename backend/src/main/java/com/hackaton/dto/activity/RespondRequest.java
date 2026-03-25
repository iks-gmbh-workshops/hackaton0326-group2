package com.hackaton.dto.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondRequest {

    @NotBlank(message = "Status is required (ACCEPTED or DECLINED)")
    private String status;
}
