package tuyenbd.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import tuyenbd.authentication.controller.dto.AuthenticationRequest;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.TokenRequest;
import tuyenbd.authentication.controller.dto.TokenValidationResponse;
import tuyenbd.authentication.domain.auth.service.AuthenticationService;
import tuyenbd.authentication.domain.auth.service.TokenService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    @MockBean
    private TokenService tokenService;

    @Test
    void authenticate_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@test.com", "password");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(authService.authenticate(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnValidationResponse() throws Exception {
        // Arrange
        TokenRequest request = new TokenRequest("valid-token");
        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .username("test@test.com")
                .roles(List.of(new SimpleGrantedAuthority("USER")))
                .build();

        when(tokenService.validateToken(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/token/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("test@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }
}
