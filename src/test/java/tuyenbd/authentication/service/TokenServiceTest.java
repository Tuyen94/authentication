package tuyenbd.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tuyenbd.authentication.controller.dto.TokenRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenStatus;
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
    void validateToken_WithValidToken_ShouldReturnValidResponse() {
        // Arrange
        String tokenValue = "valid-token";
        String email = "test@test.com";
        TokenRequest request = new TokenRequest(tokenValue);
        
        User user = User.builder()
                .email(email)
                .role(Role.USER)
                .build();
        
        Token token = Token.builder()
                .token(tokenValue)
                .status(TokenStatus.ACTIVE)
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
        TokenRequest request = new TokenRequest(tokenValue);
        
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
        TokenRequest request = new TokenRequest(tokenValue);

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
            Token.builder().token("token1").build(),
            Token.builder().token("token2").build()
        );

        when(tokenRepository.findAllActiveTokensByUser(user.getId())).thenReturn(validTokens);

        // Act
        tokenService.revokeAllUserTokens(user);

        // Assert
        verify(tokenRepository).saveAll(validTokens);
        validTokens.forEach(token -> assertSame(TokenStatus.ACTIVE, token.getStatus()));
    }

    @Test
    void disableToken_ShouldDelegateToLogoutService() {
        // Arrange
        TokenRequest request = new TokenRequest("token-to-disable");

        // Act
        tokenService.disableToken(request);

        // Assert
        verify(logoutService).logout(request.getToken());
    }
}