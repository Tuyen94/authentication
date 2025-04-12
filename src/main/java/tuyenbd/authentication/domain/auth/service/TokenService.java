package tuyenbd.authentication.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.user.entity.User;

import java.io.IOException;

public interface TokenService {

    Token getToken(String jwt, TokenType tokenType);

    AuthenticationResponse createToken(User user);

    AuthenticationResponse refreshToken(TokenRequest request);

    void revokeAllUserTokens(User user);

    TokenValidationResponse validateToken(TokenRequest request);

    boolean isTokenValid(Token token);

    void disableToken(TokenRequest request);

    void disableToken(String token);
}