package com.eaglebank.exception.handler;

import com.eaglebank.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String TYPE_BASE_URL = "https://eaglebank.com/errors";
    private static final String TIMESTAMP_KEY = "timestamp";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/not-found"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ProblemDetail handleInvalidRequestException(InvalidRequestException ex) {
        log.warn("Invalid request attempt: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid user details provided."
        );
        problemDetail.setTitle("Invalid Request");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/invalid-request"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex) {
        log.warn("Forbidden access attempt: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/forbidden"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation failed for request: {}", fieldErrors);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields"
        );
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/validation-failed"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        List<Map<String, String>> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, String> detail = new HashMap<>();
                    detail.put("field", error.getField());
                    detail.put("message", error.getDefaultMessage());
                    detail.put("type", error.getCode());
                    return detail;
                })
                .collect(Collectors.toList());

        problemDetail.setProperty("details", details);

        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Invalid or missing request body: {}", ex.getMessage());

        String detail = "Required request body is missing or malformed";

        // Provide more specific error messages
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("JSON parse error")) {
                detail = "Invalid JSON format in request body";
            } else if (ex.getMessage().contains("Required request body is missing")) {
                detail = "Request body is required";
            }
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                detail
        );
        problemDetail.setTitle("Invalid Request Body");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/invalid-body"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }

    // 400 - Custom Bad Request with details
    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/bad-request"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            problemDetail.setProperty("details", ex.getDetails());
        }

        return problemDetail;
    }

    // 401 - Authentication Failed
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Access token is missing or invalid"
        );
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/unauthorized"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Login failed: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );
        problemDetail.setTitle("Login Failed");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/unauthorized"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }

    // 403 - Access Denied (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource"
        );
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/access-denied"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }

    // 500 - Internal Server Error
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create(TYPE_BASE_URL + "/internal-error"));
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());

        return problemDetail;
    }
}