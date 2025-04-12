package tuyenbd.authentication.domain.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.auth.service.JwtService;
import tuyenbd.authentication.domain.auth.service.LogoutService;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final LogoutService logoutService;
    private final ObjectMapper objectMapper;

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
        String accessToken = generateAccessToken(user);
        saveUserToken(user, accessToken);
        return accessToken;
    }

    private String createRefreshToken(User user) {
        String refreshToken = generateRefreshToken(user);
        saveUserToken(user, refreshToken);
        return refreshToken;
    }

    public String generateAccessToken(User user) {
        return jwtService.generateToken(user);
    }

    public String generateRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }

    public void saveUserToken(User user, String tokenValue) {
        Token token = Token.builder()
                .user(user)
                .token(tokenValue)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
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
        Token token = getToken(refreshToken);
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

    @Override
    public void revokeAllUserTokens(User user) {
        var validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validTokens.isEmpty()) return;

        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    @Override
    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        String jwt = request.getToken();
        Token token = getToken(jwt);
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
        return jwtService.isTokenValid(token.getToken(), token.getUser()) && !token.isExpired() && !token.isRevoked();
    }

    @Override
    public void disableToken(TokenValidationRequest request) {
        String token = request.getToken();
        logoutService.logout(token);
    }
}