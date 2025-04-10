package tuyenbd.authentication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuyenbd.authentication.dto.TokenValidationRequest;
import tuyenbd.authentication.dto.TokenValidationResponse;
import tuyenbd.authentication.service.TokenValidationService;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class ValidateTokenController {

    private final TokenValidationService tokenValidationService;

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest request
    ) {
        return ResponseEntity.ok(tokenValidationService.validateToken(request));
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disableToken(
            @RequestBody TokenValidationRequest request
    ) {
        tokenValidationService.disableToken(request);
        return ResponseEntity.ok().build();
    }
}