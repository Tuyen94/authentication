package tuyenbd.authentication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.service.AuthenticationService;
import tuyenbd.authentication.domain.auth.service.TokenService;

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
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody TokenRequest request) {
        log.info("Processing token refresh request");
        AuthenticationResponse response = tokenService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenRequest request) {
        log.info("Validating token");
        TokenValidationResponse response = tokenService.validateToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/disable")
    public ResponseEntity<Void> disableToken(@RequestBody TokenRequest request) {
        log.info("Disabling token");
        tokenService.disableToken(request);
        return ResponseEntity.ok().build();
    }
}

