package com.eaglebank.service;


import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.exception.InvalidRequestException;
import com.eaglebank.exception.ResourceNotFoundException;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest userRequest) {
        log.info("Creating user with email: {}", userRequest.email());
        if (userRepository.existsByEmail(userRequest.email())){
            log.info("Attempt to create user with existing email: {}", userRequest.email());
            throw new InvalidRequestException("Invalid user details provided.");
        }

        String hashedPassword = passwordEncoder.encode(userRequest.password());
        var address = userRequest.address();

        User user = User.builder()
                .name(userRequest.name())
                .passwordHash(hashedPassword)
                .email(userRequest.email())
                .phoneNumber(userRequest.phoneNumber())
                .addressLine1(address.line1())
                .addressLine2(address.line2())
                .addressLine3(address.line3())
                .addressTown(address.town())
                .addressCounty(address.county())
                .addressPostcode(address.postcode())
                .build();

        User savedUser =  userRepository.save(user);
        log.info("User created. ID: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }

    public UserResponse getUserByEmail(String email) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserResponse.from(user);
    }
}
