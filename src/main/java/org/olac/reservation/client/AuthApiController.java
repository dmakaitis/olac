package org.olac.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.model.ValidateUserResponse;
import org.olac.reservation.utility.spring.JwtUtility;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthApiController {

    public static final String COOKIE_SHOW_LOGIN = "ShowLoginButton";
    public static final String COOKIE_VALUE_YES = "Y";

    private final SecurityUtility securityUtility;
    private final JwtUtility jwtUtility;
    private final UserDetailsService userDetailsService;

    @GetMapping("who-am-i")
    public ResponseEntity<WhoAmIResponse> whoAmI() {
        log.debug("Retrieving current user identity");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(new WhoAmIResponse(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        ));
    }

    @PostMapping("google-id")
    public ResponseEntity<AuthenticationResponse> authenticateUsingGoogleIdentity(@RequestBody CredentialResponse response, HttpServletResponse httpResponse) throws GeneralSecurityException, IOException {
        log.debug("Authenticating using Google Identity: {}", response);
        ValidateUserResponse validateUserResponse = securityUtility.validateUserWithGoogleIdentity(response.getCredential());
        UserDetails userDetails = userDetailsService.loadUserByUsername(validateUserResponse.getUsername());

        ResponseEntity<AuthenticationResponse> okResponse = ResponseEntity.ok(new AuthenticationResponse(
                validateUserResponse.getJwtToken(),
                validateUserResponse.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        ));

        // Set a cookie so in the future we'll know if we should display the login button...
        Cookie cookie = new Cookie(COOKIE_SHOW_LOGIN, COOKIE_VALUE_YES);
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(180));
        cookie.setSecure(false);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);

        return okResponse;
    }

    @Data
    @AllArgsConstructor
    public static class WhoAmIResponse {
        private String username;
        private List<String> grants;
    }

    @Data
    @AllArgsConstructor
    public static class AuthenticationResponse {
        private String jwtToken;
        private String username;
        private List<String> grants;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CredentialResponse {
        private String credential;
        @JsonProperty("select_by")
        private String selectBy;
    }

}
