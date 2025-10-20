package com.eaglebank.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank(message = "Name is required.")
        @Size(min = 2, max = 70, message = "Name must be between 2 and 70 characters.")
        String name,

        @NotNull(message = "Address is required.")
        @Valid
        AddressDTO address,

        @NotBlank(message = "Email is required.")
        @Email(message = "A valid email is required.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters.")
        String password,

        @NotBlank(message = "Phone number is required.")
        @Pattern(
                message = "Phone number must be in E.164 format (e.g., +442071234567).",
                regexp = "^\\+[1-9]\\d{1,14}$"
        )
        String phoneNumber

){}
