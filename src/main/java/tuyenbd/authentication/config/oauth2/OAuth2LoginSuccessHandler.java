package tuyenbd.authentication.config.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tuyenbd.authentication.entity.Role;
import tuyenbd.authentication.entity.User;
import tuyenbd.authentication.service.AuthenticationService;
import tuyenbd.authentication.service.UserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        if (!userService.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .firstname(firstName)
                    .lastname(lastName)
                    .role(Role.USER)
                    .build();
            userService.createUser(user);
        }

        var tokens = authenticationService.createTokens(email);
        var redirectUrl = "/oauth2/redirect?access_token=" + tokens.getAccessToken() + 
                         "&refresh_token=" + tokens.getRefreshToken();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}