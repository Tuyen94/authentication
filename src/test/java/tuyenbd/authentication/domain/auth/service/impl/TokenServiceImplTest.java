package tuyenbd.authentication.domain.auth.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenStatus;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.auth.service.JwtService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.enums.Role;
import tuyenbd.authentication.exception.TokenNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenServiceImplTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenServiceImpl tokenServiceSelf;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        var selfField = TokenServiceImpl.class.getDeclaredField("self");
        selfField.setAccessible(true);
        selfField.set(tokenService, tokenServiceSelf);
    }

    @Test
    void getToken_ShouldReturnToken() {
        // Given
        String jwt = "test.jwt.token";
        TokenType tokenType = TokenType.ACCESS;
        Token expectedToken = Token.builder()
                .token(jwt)
                .tokenType(tokenType)
                .build();
        when(tokenRepository.findByTokenAndTokenType(jwt, tokenType))
                .thenReturn(Optional.of(expectedToken));

        // When
        Token result = tokenService.getToken(jwt, tokenType);

        // Then
        assertNotNull(result);
        assertEquals(jwt, result.getToken());
        assertEquals(tokenType, result.getTokenType());
    }

    @Test
    void getToken_WhenNotFound_ShouldThrowException() {
        // Given
        String jwt = "invalid.jwt.token";
        TokenType tokenType = TokenType.ACCESS;
        when(tokenRepository.findByTokenAndTokenType(jwt, tokenType))
                .thenReturn(Optional.empty());

        // When/Then
        assertThrows(TokenNotFoundException.class,
                () -> tokenService.getToken(jwt, tokenType));
    }

    @Test
    void createToken_ShouldCreateBothTokens() {
        // Given
        User user = new User();
        String accessToken = "access.token";
        String refreshToken = "refresh.token";
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        // When
        AuthenticationResponse response = tokenService.createToken(user);

        // Then
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(tokenRepository, times(2)).save(any(Token.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() {
        // Given
        String refreshToken = "refresh.token";
        TokenRequest request = new TokenRequest(refreshToken);
        User user = new User();
        Token token = Token.builder()
                .token(refreshToken)
                .tokenType(TokenType.REFRESH)
                .status(TokenStatus.ACTIVE)
                .user(user)
                .build();
        String newAccessToken = "new.access.token";

        when(tokenServiceSelf.getToken(refreshToken, TokenType.REFRESH)).thenReturn(token);
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(newAccessToken);

        // When
        AuthenticationResponse response = tokenService.refreshToken(request);

        // Then
        assertNotNull(response);
        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnValidResponse() {
        // Given
        String jwt = "valid.token";
        TokenRequest request = new TokenRequest(jwt);
        User user = new User();
        user.setRole(Role.USER);
        Token token = Token.builder()
                .token(jwt)
                .tokenType(TokenType.ACCESS)
                .status(TokenStatus.ACTIVE)
                .user(user)
                .build();

        when(tokenServiceSelf.getToken(jwt, TokenType.ACCESS)).thenReturn(token);
        when(jwtService.isTokenValid(jwt, user)).thenReturn(true);

        // When
        TokenValidationResponse response = tokenService.validateToken(request);

        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals(user.getUsername(), response.getUsername());
    }

    @Test
    void disableToken_ShouldMarkTokenRequestAsInactive() {
        // Given
        String jwt = "token.to.disable";
        Token token = Token.builder()
                .token(jwt)
                .status(TokenStatus.ACTIVE)
                .build();
        when(tokenServiceSelf.getToken(jwt, TokenType.ACCESS)).thenReturn(token);

        // When
        tokenService.disableToken(jwt);

        // Then
        assertEquals(TokenStatus.INACTIVE, token.getStatus());
        verify(tokenRepository).save(token);
        verify(tokenServiceSelf).clearTokenCache(token);
    }

    @Test
    void revokeAllUserTokens_ShouldRevokeAllActiveTokens() {
        // Given
        User user = new User();
        user.setId(1L);
        List<Token> activeTokens = Arrays.asList(
            Token.builder().token("1").tokenType(TokenType.ACCESS).status(TokenStatus.ACTIVE).build(),
            Token.builder().token("2").tokenType(TokenType.REFRESH).status(TokenStatus.ACTIVE).build()
        );
        when(tokenRepository.findAllActiveTokensByUser(user.getId())).thenReturn(activeTokens);

        // When
        tokenService.revokeAllUserTokens(user);

        // Then
        verify(tokenRepository).saveAll(activeTokens);
        activeTokens.forEach(token -> {
            assertEquals(TokenStatus.INACTIVE, token.getStatus());
            verify(tokenServiceSelf).clearTokenCache(token);
        });
    }

    @Test
    void revokeAllUserTokens_WithNoActiveTokens_ShouldDoNothing() {
        // Given
        User user = new User();
        user.setId(1L);
        when(tokenRepository.findAllActiveTokensByUser(user.getId())).thenReturn(Collections.emptyList());

        // When
        tokenService.revokeAllUserTokens(user);

        // Then
        verify(tokenRepository, never()).saveAll(any());
        verify(tokenServiceSelf, never()).clearTokenCache(any());
    }
}