package tuyenbd.authentication.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.user.entity.User;

import java.io.IOException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse createTokens(String email);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void revokeAllUserTokens(User user);

    TokenValidationResponse validateToken(TokenValidationRequest request);

    void disableToken(TokenValidationRequest request);
}