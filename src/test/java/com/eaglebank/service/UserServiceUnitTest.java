package com.eaglebank.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


import com.eaglebank.dto.AddressDTO;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private User user;

    @BeforeEach
    void setUp(){
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

        user = User.builder()
                .id("usr-123abc")
                .name("John Doe")
                .email("john.doe@example.com")
                .passwordHash("hashedPassword")
                .phoneNumber("+447700900000")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .addressLine3(null)
                .addressTown("London")
                .addressCounty("Greater London")
                .addressPostcode("SW1A 1AA")
                .createdTimestamp(Instant.now())
                .updatedTimestamp(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should create user successfully with valid data")
    void createUser_WithValidData_ShouldReturnUserResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getName(), response.name());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getPhoneNumber(), response.phoneNumber());
        assertEquals(user.getAddressLine1(), response.address().line1());
        assertEquals(user.getAddressTown(), response.address().town());

        verify(userRepository, times(1)).existsByEmail(createUserRequest.email());
        verify(passwordEncoder, times(1)).encode(createUserRequest.password());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
