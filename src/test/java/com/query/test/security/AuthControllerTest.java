package com.query.test.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtUtil jwtUtil;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authenticationManager, jwtUtil, userDetailsService);
    }

    @Test
    void createAuthToken_shouldReturnToken() {
        when(jwtUtil.generateToken(anyString())).thenReturn("token");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(org.springframework.security.core.userdetails.User.withUsername("user").password("pass").roles("USER").build());
        // Since createAuthToken returns String, not ResponseEntity
        String token = authController.createAuthToken("user", "pass");
        assertTrue(token.contains("token"));
    }
}
