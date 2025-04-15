package tuyenbd.authentication.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.domain.auth.service.AuthenticationService;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthenticationResponse login(AuthenticationRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        validateRequest(request);
        authenticateCredentials(request.getEmail(), request.getPassword());
        User user = getUserByEmail(request.getEmail());
        log.info("User {} successfully authenticated", request.getEmail());

        return tokenService.createToken(user);
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("Processing logout request");
            tokenService.disableToken(token);
            log.info("Logout successful");
        } else {
            log.warn("Logout attempted with invalid authorization header");
        }
    }

    private void validateRequest(AuthenticationRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            log.error("Login attempt with invalid email");
            throw new IllegalArgumentException("Invalid email");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            log.error("Login attempt with invalid password for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid password");
        }
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
}




