package com.eaglebank.controller;


import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.security.Utils;
import com.eaglebank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest userRequest){
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        String authUserId = Utils.getAuthenticatedUserId();
        if(!userId.equals(authUserId)){
            throw new AccessDeniedException
                    ("Not authorized to perform this action");
        }

        UserResponse response = userService.getUserById(userId);

        return ResponseEntity.ok(response);
    }

}
