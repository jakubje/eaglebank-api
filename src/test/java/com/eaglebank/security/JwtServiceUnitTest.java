package com.eaglebank.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtServiceUnitTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SECRET = "20d05c6b1898cdceef37a138527dd461cfe11d068cc6b36f6b40d9ae47a333f5";
    private static final long TEST_EXPIRATION_MS = 1800000L; // 30 minutes

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", TEST_EXPIRATION_MS);

        userDetails = User.builder()
                .username(TEST_EMAIL)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_ValidEmail_ShouldReturnNonEmptyToken() {
        String token = jwtService.generateToken(TEST_EMAIL);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void generateToken_DifferentEmails_ShouldGenerateDifferentTokens() {
        String token1 = jwtService.generateToken("user1@example.com");
        String token2 = jwtService.generateToken("user2@example.com");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractUsername_ValidToken_ShouldReturnCorrectEmail() {
        String token = jwtService.generateToken(TEST_EMAIL);

        String extractedEmail = jwtService.extractUsername(token);

        assertThat(extractedEmail).isEqualTo(TEST_EMAIL);
    }

    @Test
    void extractUsername_InvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.jwt.token";

        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_TamperedToken_ShouldThrowSignatureException() {
        String token = jwtService.generateToken(TEST_EMAIL);
        String tamperedToken = token.substring(0, token.length() - 10) + "tampered123";

        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void isTokenValid_ValidTokenAndMatchingUserDetails_ShouldReturnTrue() {
        String token = jwtService.generateToken(TEST_EMAIL);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ValidTokenButDifferentUserDetails_ShouldReturnFalse() {
        String token = jwtService.generateToken(TEST_EMAIL);

        UserDetails differentUser = User.builder()
                .username("different@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_ExpiredToken_ShouldReturnFalse() {
        // Create a service with very short expiration
        JwtService shortExpirationService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortExpirationService, "jwtExpirationMs", 1L); // 1ms

        String token = shortExpirationService.generateToken(TEST_EMAIL);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> shortExpirationService.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void isTokenValid_TokenFromDifferentSecret_ShouldThrowSignatureException() {
        String token = jwtService.generateToken(TEST_EMAIL);

        // Create service with different secret
        JwtService differentSecretService = new JwtService();
        ReflectionTestUtils.setField(differentSecretService, "secret",
                "differentSecretKey1234567890abcdefghijklmnopqrstuvwxyz12345678");
        ReflectionTestUtils.setField(differentSecretService, "jwtExpirationMs", TEST_EXPIRATION_MS);

        assertThatThrownBy(() -> differentSecretService.isTokenValid(token, userDetails))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void generateToken_SameEmailCalledTwice_ShouldGenerateDifferentTokens() {
        // Tokens should be different due to different timestamps
        String token1 = jwtService.generateToken(TEST_EMAIL);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtService.generateToken(TEST_EMAIL);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractUsername_MultipleExtractionsFromSameToken_ShouldReturnSameValue() {
        String token = jwtService.generateToken(TEST_EMAIL);

        String extracted1 = jwtService.extractUsername(token);
        String extracted2 = jwtService.extractUsername(token);
        String extracted3 = jwtService.extractUsername(token);

        assertThat(extracted1).isEqualTo(TEST_EMAIL);
        assertThat(extracted2).isEqualTo(TEST_EMAIL);
        assertThat(extracted3).isEqualTo(TEST_EMAIL);
    }

    @Test
    void isTokenValid_NullToken_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.isTokenValid(null, userDetails))
                .isInstanceOf(Exception.class);
    }

    @Test
    void isTokenValid_EmptyToken_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.isTokenValid("", userDetails))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_NullToken_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.extractUsername(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_EmailWithSpecialCharacters_ShouldGenerateValidToken() {
        String emailWithSpecialChars = "test+special@example.co.uk";

        String token = jwtService.generateToken(emailWithSpecialChars);
        String extractedEmail = jwtService.extractUsername(token);

        assertThat(extractedEmail).isEqualTo(emailWithSpecialChars);
    }
}
