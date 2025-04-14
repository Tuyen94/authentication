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
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        validateRequest(request);
        authenticateCredentials(request.getEmail(), request.getPassword());
        User user = getUserByEmail(request.getEmail());

        return tokenService.createToken(user);
    }

    private void validateRequest(AuthenticationRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
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
