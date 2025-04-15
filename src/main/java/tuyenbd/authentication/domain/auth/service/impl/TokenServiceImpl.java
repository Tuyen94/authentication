package tuyenbd.authentication.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenStatus;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.auth.service.JwtService;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.exception.TokenNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    @Lazy
    @Autowired
    private TokenServiceImpl self;

    @Cacheable(cacheNames = "token", key = "#jwt + #tokenType")
    @Override
    public Token getToken(String jwt, TokenType tokenType) {
        log.info("Get Token {}", tokenType);
        return tokenRepository.findByTokenAndTokenType(jwt, tokenType)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));
    }

    @Override
    @Transactional
    public AuthenticationResponse createToken(User user) {
        log.debug("Creating new tokens for user: {}", user.getEmail());
        revokeAllUserTokens(user);

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);
        log.info("Successfully created tokens for user: {}", user.getEmail());

        return buildAuthResponse(accessToken, refreshToken);
    }

    private String createAccessToken(User user) {
        log.debug("Generating access token for user: {}", user.getEmail());
        String accessToken = jwtService.generateToken(user);
        saveUserToken(user, accessToken, TokenType.ACCESS);
        return accessToken;
    }

    private String createRefreshToken(User user) {
        log.debug("Generating refresh token for user: {}", user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, refreshToken, TokenType.REFRESH);
        return refreshToken;
    }

    public void saveUserToken(User user, String tokenValue, TokenType tokenType) {
        log.info("Save token {}", tokenType);
        Token token = Token.builder()
                .user(user)
                .token(tokenValue)
                .tokenType(tokenType)
                .build();
        tokenRepository.save(token);
    }

    @Transactional
    @Override
    public AuthenticationResponse refreshToken(TokenRequest request) {
        log.debug("Processing token refresh request");
        Token token = self.getToken(request.getToken(), TokenType.REFRESH);
        if (!isTokenValid(token)) {
            log.warn("Invalid refresh token attempt for user: {}", token.getUser().getEmail());
            throw new IllegalArgumentException("Invalid refresh token");
        }

        User user = token.getUser();
        revokeAllUserTokens(user);
        String newAccessToken = createAccessToken(user);
        log.info("Successfully refreshed tokens for user: {}", user.getEmail());
        return buildAuthResponse(newAccessToken, request.getToken());
    }

    private AuthenticationResponse buildAuthResponse(String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    @Override
    public void revokeAllUserTokens(User user) {
        log.debug("Revoke all token start {}", user.getEmail());
        var validTokens = tokenRepository.findAllActiveTokensByUser(user.getId());
        if (validTokens.isEmpty()) return;

        validTokens.forEach(token -> token.setStatus(TokenStatus.INACTIVE));
        tokenRepository.saveAll(validTokens);

        validTokens.forEach(token -> self.clearTokenCache(token));
        log.info("Revoke all token done {}", user.getEmail());
    }

    @Override
    public TokenValidationResponse validateToken(TokenRequest request) {
        log.debug("Validating token request");
        String jwt = request.getToken();
        Token token = self.getToken(jwt, TokenType.ACCESS);
        User user = token.getUser();

        boolean isValid = isTokenValid(token);
        log.info("Token validation result for user {}: {}", user.getEmail(), isValid);

        return TokenValidationResponse.builder()
                .valid(isValid)
                .username(user.getUsername())
                .roles(user.getAuthorities())
                .build();
    }

    @Override
    public boolean isTokenValid(Token token) {
        boolean isValid = jwtService.isTokenValid(token.getToken(), token.getUser()) && token.getStatus() == TokenStatus.ACTIVE;
        if (!isValid) {
            log.debug("Token invalid for user: {}. Status: {}", token.getUser().getEmail(), token.getStatus());
        }
        return isValid;
    }

    @Override
    public void disableTokenRequest(TokenRequest request) {
        String token = request.getToken();
        self.disableToken(token);
    }

    @Transactional
    @Override
    public void disableToken(String jwt) {
        log.debug("Disable token start");
        Token token = self.getToken(jwt, TokenType.ACCESS);
        markTokenAsRevoked(token);
        SecurityContextHolder.clearContext();
        log.info("Disable token done");
    }

    private void markTokenAsRevoked(Token token) {
        token.setStatus(TokenStatus.INACTIVE);
        tokenRepository.save(token);
        self.clearTokenCache(token);
    }

    @CacheEvict(cacheNames = "token", key = "#token.token + #token.tokenType")
    public void clearTokenCache(Token token) {
        log.info("Clear token cache {}", token.getTokenType());
    }
}


