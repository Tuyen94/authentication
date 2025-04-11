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
import tuyenbd.authentication.dto.AuthenticationRequest;
import tuyenbd.authentication.dto.AuthenticationResponse;
import tuyenbd.authentication.dto.RegisterRequest;
import tuyenbd.authentication.service.AuthenticationService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        log.info("Processing registration request for user: {}", request);
        AuthenticationResponse response = service.register(request);
        log.info("Successfully registered user: {}", request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        log.info("Processing authentication request for user: {}", request);
        AuthenticationResponse response = service.authenticate(request);
        log.info("Successfully authenticated user: {}", request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.info("Processing token refresh request");
        service.refreshToken(request, response);
        log.info("Successfully refreshed token");
    }
}
