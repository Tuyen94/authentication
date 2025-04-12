package tuyenbd.authentication.domain.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tuyenbd.authentication.controller.dto.AuthenticationResponse;
import tuyenbd.authentication.controller.dto.RegisterRequest;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.enums.Role;
import tuyenbd.authentication.domain.user.service.UserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = getUser(oAuth2User);

        AuthenticationResponse token = tokenService.createToken(user);

        var redirectUrl = "/oauth2/redirect?access_token=" + token.getAccessToken() +
                "&refresh_token=" + token.getRefreshToken();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private User getUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        if (userService.existsByEmail(email)) {
            return userService.getUserByEmail(email);
        }

        return userService.createUser(RegisterRequest.builder()
                .email(email)
                .firstname(firstName)
                .lastname(lastName)
                .role(Role.USER)
                .build());
    }
}
