package com.eaglebank.dto;

public record LoginResponse (
        String token,
        String type
){
}
