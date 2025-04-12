package tuyenbd.authentication.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.user.entity.User;

import java.io.IOException;

public interface TokenService {

    Token getToken(String jwt);

    AuthenticationResponse createToken(User user);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void revokeAllUserTokens(User user);

    TokenValidationResponse validateToken(TokenValidationRequest request);

    boolean isTokenValid(Token token);

    void disableToken(TokenValidationRequest request);

    void disableToken(String token);
}