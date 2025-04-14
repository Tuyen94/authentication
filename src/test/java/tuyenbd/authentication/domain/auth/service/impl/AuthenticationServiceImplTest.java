package tuyenbd.authentication.domain.auth.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private AuthenticationRequest validRequest;
    private User validUser;
    private AuthenticationResponse expectedResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validRequest = new AuthenticationRequest("test@example.com", "password123");
        validUser = new User();
        validUser.setEmail("test@example.com");
        expectedResponse = AuthenticationResponse.builder()
                .accessToken("access.token")
                .refreshToken("refresh.token")
                .build();
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnAuthenticationResponse() {
        // Given
        when(userRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(validUser));
        when(tokenService.createToken(validUser)).thenReturn(expectedResponse);

        // When
        AuthenticationResponse response = authenticationService.authenticate(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(expectedResponse.getAccessToken(), response.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), response.getRefreshToken());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(validRequest.getEmail(), validRequest.getPassword())
        );
        verify(userRepository).findByEmail(validRequest.getEmail());
        verify(tokenService).createToken(validUser);
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When/Then
        assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate(validRequest));
        verify(userRepository, never()).findByEmail(any());
        verify(tokenService, never()).createToken(any());
    }

    @Test
    void authenticate_WithNonExistentUser_ShouldThrowUsernameNotFoundException() {
        // Given
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.authenticate(validRequest));
        verify(tokenService, never()).createToken(any());
    }

    @Test
    void authenticate_ShouldHandleNullEmail() {
        // Given
        AuthenticationRequest requestWithNullEmail = new AuthenticationRequest(null, "password123");

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate(requestWithNullEmail));
        verifyNoInteractions(userRepository, tokenService);
    }

    @Test
    void authenticate_ShouldHandleNullPassword() {
        // Given
        AuthenticationRequest requestWithNullPassword = new AuthenticationRequest("test@example.com", null);

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate(requestWithNullPassword));
        verifyNoInteractions(userRepository, tokenService);
    }

    @Test
    void authenticate_ShouldHandleEmptyEmail() {
        // Given
        AuthenticationRequest requestWithEmptyEmail = new AuthenticationRequest("", "password123");

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate(requestWithEmptyEmail));
        verifyNoInteractions(userRepository, tokenService);
    }

    @Test
    void authenticate_ShouldHandleEmptyPassword() {
        // Given
        AuthenticationRequest requestWithEmptyPassword = new AuthenticationRequest("test@example.com", "");

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate(requestWithEmptyPassword));
        verifyNoInteractions(userRepository, tokenService);
    }
}