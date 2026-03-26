package com.hackaton.controller;

import com.hackaton.config.SecurityConfig;
import com.hackaton.dto.user.UserResponse;
import com.hackaton.model.User;
import com.hackaton.model.enums.UserRole;
import com.hackaton.security.CustomUserDetails;
import com.hackaton.security.JwtAuthenticationFilter;
import com.hackaton.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setupFilterPassThrough() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void getProfile_withoutAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_withAuthenticatedUser_returnsOk() throws Exception {
        User user = User.builder()
                .id(7L)
                .email("user@test.com")
                .displayName("User")
                .passwordHash("encoded")
                .role(UserRole.USER)
                .agbAccepted(true)
                .build();

        CustomUserDetails principal = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        UserResponse response = UserResponse.builder()
                .id(7L)
                .email("user@test.com")
                .displayName("User")
                .role("USER")
                .agbAccepted(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getProfile(7L)).thenReturn(response);

        mockMvc.perform(get("/api/users/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getProfile(7L);
    }
}
