package com.query.test.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {
    @Test
    void generateToken_shouldReturnValidToken() {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken("user");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken("user");
        String username = jwtUtil.extractUsername(token);
        assertEquals("user", username);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken("user");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken("user");
        assertFalse(jwtUtil.validateToken(token + "invalid"));
    }
}
