package com.eaglebank.controller;


import com.eaglebank.dto.AddressDTO;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.security.JwtService;
import com.eaglebank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        AddressDTO testAddress = new AddressDTO(
                "123 Main St",
                "Apt 4B",
                null,
                "London",
                "Greater London",
                "SW1A 1AA"
        );

        createUserRequest = new CreateUserRequest(
                "John Doe",
                testAddress,
                "john.doe@example.com",
                "password123",
                "+447700900000"
        );

        userResponse = new UserResponse(
                "usr-123abc",
                "John Doe",
                testAddress,
                "+447700900000",
                "john.doe@example.com",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("POST /v1/users - Should create user and return 201 with valid data")
    void createUser_WithValidData_ShouldReturn201Created() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("usr-123abc"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+447700900000"))
                .andExpect(jsonPath("$.address.line1").value("123 Main St"))
                .andExpect(jsonPath("$.address.town").value("London"))
                .andExpect(jsonPath("$.address.county").value("Greater London"))
                .andExpect(jsonPath("$.address.postcode").value("SW1A 1AA"))
                .andExpect(jsonPath("$.createdTimestamp").exists())
                .andExpect(jsonPath("$.updatedTimestamp").exists());

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /v1/users - Should return 400 when address is missing")
    void createUser_WithMissingAddress_ShouldReturn400() throws Exception {
        // Arrange
        CreateUserRequest invalidRequest = new CreateUserRequest(
                "John Doe",
                null, // Missing address
                "john.doe@example.com",
                "password123",
                "+447700900000"
        );

        // Act & Assert
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").exists());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /v1/users - Should return 400 when address line1 is missing")
    void createUser_WithMissingAddressLine1_ShouldReturn400() throws Exception {
        // Arrange
        AddressDTO invalidAddress = new AddressDTO(
                null, // Missing line1
                "Apt 4B",
                null,
                "London",
                "Greater London",
                "SW1A 1AA"
        );
        CreateUserRequest invalidRequest = new CreateUserRequest(
                "John Doe",
                invalidAddress,
                "john.doe@example.com",
                "password123",
                "+447700900000"
        );

        // Act & Assert
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").exists());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /v1/users - Should return 400 when address line1 is blank")
    void createUser_WithBlankAddressLine1_ShouldReturn400() throws Exception {
        // Arrange
        AddressDTO invalidAddress = new AddressDTO(
                "   ", // Blank line1
                "Apt 4B",
                null,
                "London",
                "Greater London",
                "SW1A 1AA"
        );
        CreateUserRequest invalidRequest = new CreateUserRequest(
                "John Doe",
                invalidAddress,
                "john.doe@example.com",
                "password123",
                "+447700900000"
        );

        // Act & Assert
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /v1/users - Should return 400 when request body is empty")
    void createUser_WithEmptyBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /v1/users - Should return 400 when request body is null")
    void createUser_WithNullBody_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

}
