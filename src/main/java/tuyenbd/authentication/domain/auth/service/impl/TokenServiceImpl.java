package tuyenbd.authentication.domain.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenStatus;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.auth.service.JwtService;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService, LogoutHandler {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Lazy
    @Autowired
    private TokenServiceImpl self;

    @Cacheable(cacheNames = "token", key = "#jwt")
    @Override
    public Token getToken(String jwt) {
        return tokenRepository.findByToken(jwt)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));
    }

    @Override
    @Transactional
    public AuthenticationResponse createToken(User user) {
        revokeAllUserTokens(user);

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(accessToken, refreshToken);
    }

    private String createAccessToken(User user) {
        String accessToken = jwtService.generateToken(user);
        saveUserToken(user, accessToken, TokenType.ACCESS);
        return accessToken;
    }

    private String createRefreshToken(User user) {
        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, refreshToken, TokenType.REFRESH);
        return refreshToken;
    }

    public void saveUserToken(User user, String tokenValue, TokenType tokenType) {
        Token token = Token.builder()
                .user(user)
                .token(tokenValue)
                .tokenType(tokenType)
                .build();
        tokenRepository.save(token);
    }

    @Transactional
    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String refreshToken = authHeader.substring(7);
        Token token = self.getToken(refreshToken);
        if (!isTokenValid(token)) {
            return;
        }

        User user = token.getUser();
        revokeAllUserTokens(user);
        String newAccessToken = createAccessToken(user);

        AuthenticationResponse authResponse = buildAuthResponse(newAccessToken, refreshToken);
        objectMapper.writeValue(response.getOutputStream(), authResponse);
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
        var validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validTokens.isEmpty()) return;

        validTokens.forEach(token -> token.setStatus(TokenStatus.INACTIVE));
        tokenRepository.saveAll(validTokens);

        validTokens.forEach(token -> self.clearTokenCache(token.getToken()));
    }

    @Override
    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        String jwt = request.getToken();
        Token token = self.getToken(jwt);
        User user = token.getUser();

        boolean isValid = isTokenValid(token);

        return TokenValidationResponse.builder()
                .valid(isValid)
                .username(user.getUsername())
                .roles(user.getAuthorities())
                .build();
    }

    @Override
    public boolean isTokenValid(Token token) {
        return jwtService.isTokenValid(token.getToken(), token.getUser()) && token.getStatus() == TokenStatus.ACTIVE;
    }

    @Override
    public void disableToken(TokenValidationRequest request) {
        String token = request.getToken();
        self.disableToken(token);
    }

    @Transactional
    @Override
    public void disableToken(String token) {
        tokenRepository.findByToken(token).ifPresentOrElse(
                storedToken -> {
                    markTokenAsRevoked(storedToken);
                    SecurityContextHolder.clearContext();
                    log.info("Successfully logged out. Token revoked for user.");
                },
                () -> log.warn("Token not found when logout")
        );
    }

    private void markTokenAsRevoked(Token token) {
        token.setStatus(TokenStatus.INACTIVE);
        tokenRepository.save(token);
        self.clearTokenCache(token.getToken());
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String token = extractTokenFromRequest(request);
        self.disableToken(token);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Logout request without Bearer token");
        }
        return authHeader.substring(7);
    }

    @CacheEvict(cacheNames = "token", key = "#jwt")
    public void clearTokenCache(String jwt) {
        log.info("Clear token cache {}", jwt);
    }
}