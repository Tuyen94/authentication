package tuyenbd.authentication.domain.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.auth.service.AuthenticationService;
import tuyenbd.authentication.domain.auth.service.JwtService;
import tuyenbd.authentication.domain.auth.service.LogoutService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.repository.UserRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final LogoutService logoutService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticateCredentials(request.getEmail(), request.getPassword());
        User user = getUserByEmail(request.getEmail());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return buildAuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthenticationResponse createTokens(String email) {
        User user = getUserByEmail(email);

        revokeAllUserTokens(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, accessToken);
        saveUserToken(user, refreshToken);

        return buildAuthResponse(accessToken, refreshToken);
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String refreshToken = authHeader.substring(7);
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            return;
        }

        User user = getUserByEmail(userEmail);
        if (!jwtService.isTokenValid(refreshToken, user)) {
            return;
        }

        String newAccessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, newAccessToken);

        AuthenticationResponse authResponse = buildAuthResponse(newAccessToken, refreshToken);
        objectMapper.writeValue(response.getOutputStream(), authResponse);
    }

    private void authenticateCredentials(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private void saveUserToken(User user, String tokenValue) {
        Token token = Token.builder()
                .user(user)
                .token(tokenValue)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validTokens.isEmpty()) return;

        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private AuthenticationResponse buildAuthResponse(String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        String token = request.getToken();
        String userEmail = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isValid = jwtService.isTokenValid(token, user) &&
                tokenRepository.findByToken(token)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);

        return TokenValidationResponse.builder()
                .valid(isValid)
                .username(userEmail)
                .roles(user.getAuthorities())
                .build();
    }

    @Override
    public void disableToken(TokenValidationRequest request) {
        String token = request.getToken();
        logoutService.logout(token);
    }
}