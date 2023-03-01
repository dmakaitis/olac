package org.olac.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.spring.JwtUtility;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SecurityUtility securityUtility;
    private final JwtUtility jwtUtility;

    @GetMapping("who-am-i")
    public ResponseEntity<WhoAmIResponse> whoAmI() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(new WhoAmIResponse(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        ));
    }

    @PostMapping("google-id")
    public ResponseEntity<String> authenticateUsingGoogleIdentity(@RequestBody CredentialResponse response) throws GeneralSecurityException, IOException {
        return ResponseEntity.ok(securityUtility.validateUserWithGoogleIdentity(response.getCredential()));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class WhoAmIResponse {
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
