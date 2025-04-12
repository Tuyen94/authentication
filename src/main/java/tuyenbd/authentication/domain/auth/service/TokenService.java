package tuyenbd.authentication.domain.auth.service;

import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.user.entity.User;

public interface TokenService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    void saveUserToken(User user, String tokenValue);

    Token getToken(String jwt);

    void revokeAllUserTokens(User user);

    TokenValidationResponse validateToken(TokenValidationRequest request);

    boolean isTokenValid(Token token);

    void disableToken(TokenValidationRequest request);
}