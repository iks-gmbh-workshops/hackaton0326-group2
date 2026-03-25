package com.hackaton.service;

import com.hackaton.dto.auth.AuthResponse;
import com.hackaton.dto.auth.LoginRequest;
import com.hackaton.dto.auth.RegisterRequest;
import com.hackaton.model.User;
import com.hackaton.model.enums.UserRole;
import com.hackaton.exception.BadRequestException;
import com.hackaton.exception.ConflictException;
import com.hackaton.exception.ResourceNotFoundException;
import com.hackaton.repository.UserRepository;
import com.hackaton.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        if (!Boolean.TRUE.equals(request.getAgbAccepted())) {
            throw new BadRequestException("AGB must be accepted");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .role(UserRole.USER)
                .agbAccepted(true)
                .build();

        userRepository.save(user);

        String token = jwtUtils.generateTokenFromEmail(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtils.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .build();
    }
}
