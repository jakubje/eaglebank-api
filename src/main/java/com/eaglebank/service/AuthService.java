package com.eaglebank.service;

import com.eaglebank.dto.LoginRequest;
import com.eaglebank.dto.LoginResponse;
import com.eaglebank.exception.InvalidCredentialsException;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(@Valid LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.email());

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            log.warn("Invalid password attempt for email: {}", loginRequest.email());
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());
        log.info("User logged in successfully: {}", user.getEmail());

        return new LoginResponse(token, "Bearer");
    }
}