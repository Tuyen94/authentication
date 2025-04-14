package tuyenbd.authentication.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.service.AuthenticationService;
import tuyenbd.authentication.domain.auth.service.TokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_ShouldReturnAuthenticationResponse() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");
        AuthenticationResponse expectedResponse = new AuthenticationResponse("token", "refreshToken");
        when(authService.authenticate(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<AuthenticationResponse> response = controller.authenticate(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(authService).authenticate(request);
    }

    @Test
    void refreshToken_ShouldReturnNewAuthenticationResponse() {
        // Given
        TokenRequest request = new TokenRequest("refreshToken");
        AuthenticationResponse expectedResponse = new AuthenticationResponse("newToken", "newRefreshToken");
        when(tokenService.refreshToken(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<AuthenticationResponse> response = controller.refreshToken(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(tokenService).refreshToken(request);
    }

    @Test
    void validateToken_ShouldReturnValidationResponse() {
        // Given
        TokenRequest request = new TokenRequest("token");
        TokenValidationResponse expectedResponse = new TokenValidationResponse();
        when(tokenService.validateToken(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<TokenValidationResponse> response = controller.validateToken(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(tokenService).validateToken(request);
    }

    @Test
    void disableToken_ShouldReturnOkResponse() {
        // Given
        TokenRequest request = new TokenRequest("token");

        // When
        ResponseEntity<Void> response = controller.disableToken(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(tokenService).disableToken(request);
    }
}