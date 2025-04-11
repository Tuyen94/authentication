package tuyenbd.authentication.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tuyenbd.authentication.controller.dto.TokenValidationRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

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

    public void disableToken(TokenValidationRequest request) {
        String token = request.getToken();
        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        
        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);
    }
}