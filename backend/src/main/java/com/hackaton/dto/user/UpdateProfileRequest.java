package com.hackaton.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Email(message = "Invalid email format")
    private String email;

    private String displayName;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
