package tuyenbd.authentication.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.service.AuthenticationService;
import tuyenbd.authentication.domain.auth.service.TokenService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    private final TokenService tokenService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        log.info("Authentication request {}", request);
        AuthenticationResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.info("Processing token refresh request");
        authService.refreshToken(request, response);
        log.info("Successfully refreshed token");
    }

    @PostMapping("/token/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest request
    ) {
        log.info("Validating token");
        TokenValidationResponse response = tokenService.validateToken(request);
        log.info("Token validation completed with status: {}", response.isValid());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/disable")
    public ResponseEntity<Void> disableToken(
            @RequestBody TokenValidationRequest request
    ) {
        log.info("Disabling token");
        tokenService.disableToken(request);
        log.info("Token successfully disabled");
        return ResponseEntity.ok().build();
    }
}

