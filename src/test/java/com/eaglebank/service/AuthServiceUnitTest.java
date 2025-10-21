package com.eaglebank.service;

import com.eaglebank.dto.LoginRequest;
import com.eaglebank.dto.LoginResponse;
import com.eaglebank.exception.InvalidCredentialsException;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("usr-12345")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .name("Test User")
                .phoneNumber("1234567890")
                .addressLine1("123 Test St")
                .addressTown("Test Town")
                .addressCounty("Test County")
                .addressPostcode("12345")
                .createdTimestamp(Instant.now())
                .updatedTimestamp(Instant.now())
                .build();

        validLoginRequest = new LoginRequest("test@example.com", "password123");
    }

    @Test
    void login_ValidCredentials_ShouldReturnLoginResponse() {
        String expectedToken = "jwt-token-12345";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("usr-12345", "test@example.com")).thenReturn(expectedToken);

        LoginResponse result = authService.login(validLoginRequest);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(expectedToken);
        assertThat(result.type()).isEqualTo("Bearer");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtService).generateToken("usr-12345", "test@example.com");
    }

    @Test
    void login_UserNotFound_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials provided");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials provided");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_NonExistentEmail_ShouldThrowInvalidCredentialsException() {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");
        
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials provided");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_EmptyPassword_ShouldStillValidateAgainstStoredHash() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials provided");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("", "$2a$10$hashedPassword");
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_JwtServiceReturnsToken_ShouldReturnCorrectResponse() {
        String customToken = "custom-jwt-token";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("usr-12345", "test@example.com")).thenReturn(customToken);

        LoginResponse result = authService.login(validLoginRequest);

        assertThat(result.token()).isEqualTo(customToken);
        assertThat(result.type()).isEqualTo("Bearer");
    }

    @Test
    void login_PasswordEncoderThrowsException_ShouldPropagateException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                .thenThrow(new RuntimeException("Password encoding error"));

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Password encoding error");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }
}
