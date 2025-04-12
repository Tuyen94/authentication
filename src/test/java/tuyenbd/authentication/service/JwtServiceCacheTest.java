package tuyenbd.authentication.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import tuyenbd.authentication.domain.auth.service.impl.JwtServiceImpl;

import java.util.ArrayList;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class JwtServiceCacheTest {

    @SpyBean
    private JwtServiceImpl jwtService;

    @Test
    void whenCallingIsTokenValidTwice_thenSecondCallIsCached() {
        // Given
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);

        // When
        jwtService.isTokenValid(token, userDetails);
        jwtService.isTokenValid(token, userDetails);

        // Then
        // Verify that extractAllClaims is called only once due to caching
        verify(jwtService, times(1)).extractClaim(token, claims -> claims.getSubject());
    }
}