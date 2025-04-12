package tuyenbd.authentication.domain.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.service.AuthenticationService;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.repository.UserRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticateCredentials(request.getEmail(), request.getPassword());
        User user = getUserByEmail(request.getEmail());

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, accessToken);

        return buildAuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthenticationResponse createTokens(String email) {
        User user = getUserByEmail(email);

        tokenService.revokeAllUserTokens(user);

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        tokenService.saveUserToken(user, accessToken);
        tokenService.saveUserToken(user, refreshToken);

        return buildAuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String refreshToken = authHeader.substring(7);
        Token token = tokenService.getToken(refreshToken);
        if (!tokenService.isTokenValid(token)) {
            return;
        }

        User user = token.getUser();

        String newAccessToken = tokenService.generateAccessToken(user);
        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, newAccessToken);

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

    private AuthenticationResponse buildAuthResponse(String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
