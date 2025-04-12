package tuyenbd.authentication.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.auth.service.JwtService;
import tuyenbd.authentication.domain.auth.service.LogoutService;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final LogoutService logoutService;

    @Override
    public String generateAccessToken(User user) {
        return jwtService.generateToken(user);
    }

    @Override
    public String generateRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }

    @Override
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

    @Override
    public Token getToken(String jwt) {
        return tokenRepository.findByToken(jwt)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));
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