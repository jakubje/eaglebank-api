package com.eaglebank.dto;

import com.eaglebank.model.User;
import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        AddressDTO address,
        String phoneNumber,
        String email,
        Instant createdTimestamp,
        Instant updatedTimestamp
) {
    public static UserResponse from(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                new AddressDTO(
                        user.getAddressLine1(),
                        user.getAddressLine2(),
                        user.getAddressLine3(),
                        user.getAddressTown(),
                        user.getAddressCounty(),
                        user.getAddressPostcode()
                ),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
    }
}
