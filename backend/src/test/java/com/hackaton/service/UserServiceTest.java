package com.hackaton.service;

import com.hackaton.dto.user.UpdateProfileRequest;
import com.hackaton.dto.user.UserResponse;
import com.hackaton.exception.ConflictException;
import com.hackaton.exception.ResourceNotFoundException;
import com.hackaton.model.User;
import com.hackaton.model.enums.UserRole;
import com.hackaton.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .passwordHash("encoded")
                .role(UserRole.USER)
                .agbAccepted(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getProfile_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getProfile(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getDisplayName()).isEqualTo("Test User");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void getProfile_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_changeDisplayName() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .displayName("New Name")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateProfile(1L, request);

        assertThat(testUser.getDisplayName()).isEqualTo("New Name");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_changeEmail_success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("new@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateProfile(1L, request);

        assertThat(testUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateProfile_duplicateEmail_throwsConflict() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("taken@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void updateProfile_changePassword() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .password("newpassword")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateProfile(1L, request);

        assertThat(testUser.getPasswordHash()).isEqualTo("new-encoded");
    }

    @Test
    void deleteAccount_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteAccount(1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteAccount_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
