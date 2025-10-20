package com.eaglebank.integration;

import com.eaglebank.dto.AddressDTO;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User API Integration Tests")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();

        AddressDTO address = new AddressDTO(
                "123 Main St",
                "Apt 4B",
                null,
                "London",
                "Greater London",
                "SW1A 1AA"
        );

        createUserRequest = new CreateUserRequest(
                "John Doe",
                address,
                "john.doe@example.com",
                "password123",
                "+447700900000"
        );
    }

    @Test
    @DisplayName("POST  /v1/users - Should create user and return 201")
    void createUser_WithValidData_ShouldReturn201() throws Exception {

        MvcResult result = mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+447700900000"))
                .andExpect(jsonPath("$.address.line1").value("123 Main St"))
                .andExpect(jsonPath("$.address.town").value("London"))
                .andExpect(jsonPath("$.createdTimestamp").exists())
                .andExpect(jsonPath("$.updatedTimestamp").exists())
                .andReturn();

        String userId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        User savedUser = userRepository.findById(userId).orElse(null);
        assertNotNull(savedUser);
        assertEquals(savedUser.getEmail(), createUserRequest.email());
        assertEquals(savedUser.getName(), createUserRequest.name());
        assertTrue(savedUser.getId().startsWith("usr-"));
    }


    @Test
    @DisplayName("POST /v1/users - Should return 400 when name is missing")
    void createUser_WithMissingName_ShouldReturn400() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest(
                null,
                createUserRequest.address(),
                createUserRequest.email(),
                createUserRequest.password(),
                createUserRequest.phoneNumber()
        );

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").exists());

        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("POST /v1/users - Should return 400 when email is invalid")
    void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest(
                createUserRequest.name(),
                createUserRequest.address(),
                "not-an-email",
                createUserRequest.password(),
                createUserRequest.phoneNumber()
        );

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        assertEquals(0, userRepository.count());
    }


}
