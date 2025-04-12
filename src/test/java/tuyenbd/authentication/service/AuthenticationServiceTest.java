package tuyenbd.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.auth.service.impl.AuthenticationServiceImpl;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnTokens() {
        // Arrange
        String email = "test@test.com";
        String password = "password";
        AuthenticationRequest request = new AuthenticationRequest(email, password);
        
        User user = User.builder()
                .email(email)
                .password(password)
                .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(tokenService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(tokenService).revokeAllUserTokens(user);
        verify(tokenService).saveUserToken(eq(user), anyString());
    }

    @Test
    void authenticate_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@test.com";
        String password = "password";
        AuthenticationRequest request = new AuthenticationRequest(email, password);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void createTokens_WithValidEmail_ShouldReturnTokens() {
        // Arrange
        String email = "test@test.com";
        User user = User.builder()
                .email(email)
                .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenService.generateAccessToken(user)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(user)).thenReturn("refresh-token");

        // Act
        AuthenticationResponse response = authenticationService.createTokens(email);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(tokenService).revokeAllUserTokens(user);
        verify(tokenService).saveUserToken(user, "access-token");
        verify(tokenService).saveUserToken(user, "refresh-token");
    }
}

