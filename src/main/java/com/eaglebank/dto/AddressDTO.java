package com.eaglebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressDTO(
        @NotBlank(message = "Address line 1 is required.")
        @Size(max = 100, message = "Address line 1 must not exceed 100 characters.")
        String line1,

        @Size(max = 100, message = "Address line 2 must not exceed 100 characters.")
        String line2,

        @Size(max = 100, message = "Address line 3 must not exceed 100 characters.")
        String line3,

        @NotBlank(message = "Town is required.")
        @Size(max = 50, message = "Town must not exceed 50 characters.")
        String town,

        @NotBlank(message = "County is required.")
        @Size(max = 50, message = "County must not exceed 50 characters.")
        String county,

        @NotBlank(message = "Postcode is required.")
        @Size(min = 2, max = 10, message = "Postcode must be between 2 and 10 characters.")
        String postcode
) {}