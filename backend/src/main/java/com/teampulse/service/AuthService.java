package com.teampulse.service;

import com.teampulse.entity.Role;
import com.teampulse.entity.User;
import com.teampulse.exception.UnauthorizedException;
import com.teampulse.graphql.input.SignUpInput;
import com.teampulse.repository.UserRepository;
import com.teampulse.security.JwtTokenProvider;
import com.teampulse.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Map<String, Object> signUp(SignUpInput input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .name(input.getName())
                .role(Role.MEMBER)
                .build();

        user = userRepository.save(user);
        return generateAuthPayload(user);
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return generateAuthPayload(user);
    }

    public Map<String, Object> refreshToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return generateAuthPayload(user);
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private Map<String, Object> generateAuthPayload(User user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return Map.of(
                "token", token,
                "refreshToken", refreshToken,
                "user", user
        );
    }
}
