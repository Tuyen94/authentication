package tuyenbd.authentication.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuyenbd.authentication.entity.LoginAttempt;
import tuyenbd.authentication.entity.User;
import tuyenbd.authentication.repository.LoginAttemptRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    private final LoginAttemptRepository loginAttemptRepository;
    
    public void recordSuccessfulAttempt(User user, HttpServletRequest request) {
        saveLoginAttempt(user, request, true, null);
    }
    
    public void recordFailedAttempt(User user, HttpServletRequest request, String reason) {
        saveLoginAttempt(user, request, false, reason);
    }
    
    private void saveLoginAttempt(User user, HttpServletRequest request, boolean successful, String failureReason) {
        LoginAttempt attempt = LoginAttempt.builder()
                .user(user)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .successful(successful)
                .timestamp(LocalDateTime.now())
                .failureReason(failureReason)
                .build();
                
        loginAttemptRepository.save(attempt);
    }
    
    public boolean isSuspiciousActivity(User user) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<LoginAttempt> recentAttempts = loginAttemptRepository
                .findByUserAndTimestampAfter(user, threshold);
                
        long failedAttempts = recentAttempts.stream()
                .filter(attempt -> !attempt.isSuccessful())
                .count();
                
        return failedAttempts >= 5;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}