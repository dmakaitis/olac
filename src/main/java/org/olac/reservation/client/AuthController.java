package org.olac.reservation.client;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.spring.JwtUtility;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityUtility securityUtility;
    private final JwtUtility jwtUtility;

    @PostMapping("login")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody LoginRequest request) {
        if (!securityUtility.validatePassword(request.getUsername(), request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(jwtUtility.generateJwtToken(request.getUsername()));
    }

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
}
