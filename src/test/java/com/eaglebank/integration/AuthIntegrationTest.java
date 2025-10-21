package com.eaglebank.integration;

import com.eaglebank.dto.LoginRequest;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = User.builder()
                .id("usr-test123")
                .email("integration@test.com")
                .passwordHash(passwordEncoder.encode("testpassword"))
                .name("Integration Test User")
                .phoneNumber("1234567890")
                .addressLine1("123 Integration St")
                .addressTown("Test Town")
                .addressCounty("Test County")
                .addressPostcode("12345")
                .createdTimestamp(Instant.now())
                .updatedTimestamp(Instant.now())
                .build();
        
        userRepository.save(testUser);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "testpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void login_WithInvalidEmail_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@test.com", "testpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "wrongpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithMalformedEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest("invalid-email", "testpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest("", "testpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithNullRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithDifferentValidUser_ShouldReturnToken() throws Exception {
        User secondUser = User.builder()
                .id("usr-test456")
                .email("second@test.com")
                .passwordHash(passwordEncoder.encode("secondpassword"))
                .name("Second Test User")
                .phoneNumber("0987654321")
                .addressLine1("456 Second St")
                .addressTown("Second Town")
                .addressCounty("Second County")
                .addressPostcode("54321")
                .createdTimestamp(Instant.now())
                .updatedTimestamp(Instant.now())
                .build();
        
        userRepository.save(secondUser);

        LoginRequest loginRequest = new LoginRequest("second@test.com", "secondpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void login_CaseInsensitiveEmail_ShouldWork() throws Exception {
        LoginRequest loginRequest = new LoginRequest("INTEGRATION@test.com", "testpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
