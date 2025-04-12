package tuyenbd.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.auth.service.JwtService;
import tuyenbd.authentication.domain.auth.service.LogoutService;
import tuyenbd.authentication.domain.auth.service.impl.TokenServiceImpl;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.enums.Role;
import tuyenbd.authentication.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private LogoutService logoutService;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateAccessToken_ShouldDelegateToJwtService() {
        // Arrange
        User user = User.builder().email("test@test.com").build();
        when(jwtService.generateToken(user)).thenReturn("access-token");

        // Act
        String token = tokenService.generateAccessToken(user);

        // Assert
        assertEquals("access-token", token);
        verify(jwtService).generateToken(user);
    }

    @Test
    void generateRefreshToken_ShouldDelegateToJwtService() {
        // Arrange
        User user = User.builder().email("test@test.com").build();
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        // Act
        String token = tokenService.generateRefreshToken(user);

        // Assert
        assertEquals("refresh-token", token);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnValidResponse() {
        // Arrange
        String tokenValue = "valid-token";
        String email = "test@test.com";
        TokenValidationRequest request = new TokenValidationRequest(tokenValue);
        
        User user = User.builder()
                .email(email)
                .role(Role.USER)
                .build();
        
        Token token = Token.builder()
                .token(tokenValue)
                .expired(false)
                .revoked(false)
                .build();

        when(jwtService.extractUsername(tokenValue)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(tokenValue, user)).thenReturn(true);
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        // Act
        TokenValidationResponse response = tokenService.validateToken(request);

        // Assert
        assertTrue(response.isValid());
        assertEquals(email, response.getUsername());
        assertEquals(Set.of("USER"), response.getRoles());
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnInvalidResponse() {
        // Arrange
        String tokenValue = "invalid-token";
        String email = "test@test.com";
        TokenValidationRequest request = new TokenValidationRequest(tokenValue);
        
        User user = User.builder()
                .email(email)
                .role(Role.USER)
                .build();

        when(jwtService.extractUsername(tokenValue)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(tokenValue, user)).thenReturn(false);

        // Act
        TokenValidationResponse response = tokenService.validateToken(request);

        // Assert
        assertFalse(response.isValid());
        assertEquals(email, response.getUsername());
        assertEquals(Set.of("USER"), response.getRoles());
    }

    @Test
    void validateToken_WithNonexistentUser_ShouldThrowException() {
        // Arrange
        String tokenValue = "token";
        String email = "nonexistent@test.com";
        TokenValidationRequest request = new TokenValidationRequest(tokenValue);

        when(jwtService.extractUsername(tokenValue)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> tokenService.validateToken(request));
    }

    @Test
    void revokeAllUserTokens_ShouldUpdateAndSaveTokens() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();
        
        List<Token> validTokens = List.of(
            Token.builder().token("token1").expired(false).revoked(false).build(),
            Token.builder().token("token2").expired(false).revoked(false).build()
        );

        when(tokenRepository.findAllValidTokensByUser(user.getId())).thenReturn(validTokens);

        // Act
        tokenService.revokeAllUserTokens(user);

        // Assert
        verify(tokenRepository).saveAll(validTokens);
        validTokens.forEach(token -> {
            assertTrue(token.isExpired());
            assertTrue(token.isRevoked());
        });
    }

    @Test
    void disableToken_ShouldDelegateToLogoutService() {
        // Arrange
        TokenValidationRequest request = new TokenValidationRequest("token-to-disable");

        // Act
        tokenService.disableToken(request);

        // Assert
        verify(logoutService).logout(request.getToken());
    }
}