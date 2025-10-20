package com.eaglebank.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BadRequestException extends RuntimeException {
    private final List<Map<String, String>> details;

    public BadRequestException(String message) {
        super(message);
        this.details = new ArrayList<>();
    }

    public BadRequestException(String message, List<Map<String, String>> details) {
        super(message);
        this.details = details;
    }

    public List<Map<String, String>> getDetails() {
        return details;
    }

    public void addDetail(String field, String message, String type) {
        Map<String, String> detail = new HashMap<>();
        detail.put("field", field);
        detail.put("message", message);
        detail.put("type", type);
        this.details.add(detail);
    }
}