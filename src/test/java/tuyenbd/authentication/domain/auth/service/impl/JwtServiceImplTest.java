package tuyenbd.authentication.domain.auth.service.impl;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    @Mock
    private UserDetails userDetails;

    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long JWT_EXPIRATION = 86400000; // 1 day
    private final long REFRESH_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
    }

    @Test
    void extractUsername_ShouldReturnUsername() {
        // Given
        String username = "test@example.com";
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtService.generateToken(userDetails);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void generateToken_WithoutExtraClaims_ShouldGenerateValidToken() {
        // Given
        String username = "test@example.com";
        when(userDetails.getUsername()).thenReturn(username);

        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void generateToken_WithExtraClaims_ShouldGenerateValidToken() {
        // Given
        String username = "test@example.com";
        when(userDetails.getUsername()).thenReturn(username);
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");

        // When
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals(username, jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractClaim(token, claims -> claims.get("role", String.class)));
    }

    @Test
    void generateRefreshToken_ShouldGenerateValidToken() {
        // Given
        String username = "test@example.com";
        when(userDetails.getUsername()).thenReturn(username);

        // When
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Then
        assertNotNull(refreshToken);
        assertTrue(jwtService.isTokenValid(refreshToken, userDetails));
        assertEquals(username, jwtService.extractUsername(refreshToken));
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        // Given
        String username = "test@example.com";
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithInvalidUsername_ShouldReturnFalse() {
        // Given
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtService.generateToken(userDetails);
        when(userDetails.getUsername()).thenReturn("different@example.com");

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractClaim_ShouldExtractSpecificClaim() {
        // Given
        String username = "test@example.com";
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtService.generateToken(userDetails);

        // When
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.after(issuedAt));
        assertTrue(expiration.getTime() - issuedAt.getTime() <= JWT_EXPIRATION);
    }
}