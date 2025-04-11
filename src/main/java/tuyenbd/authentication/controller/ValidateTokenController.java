package tuyenbd.authentication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.service.TokenValidationService;

@Slf4j
@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class ValidateTokenController {

    private final TokenValidationService tokenValidationService;

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest request
    ) {
        log.info("Validating token");
        TokenValidationResponse response = tokenValidationService.validateToken(request);
        log.info("Token validation completed with status: {}", response.isValid());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disableToken(
            @RequestBody TokenValidationRequest request
    ) {
        log.info("Disabling token");
        tokenValidationService.disableToken(request);
        log.info("Token successfully disabled");
        return ResponseEntity.ok().build();
    }
}
