package org.olac.reservation.utility.spring;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtUtility {

    private String jwtSecret = "my-jwt-secret-with-an-even-longer-key";
    private long jwtExpirationMs = TimeUnit.MINUTES.toMillis(120);

    private Key getKey() {
        return Keys.hmacShaKeyFor((jwtSecret + jwtSecret).getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Optional<String> validateJwtToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            log.debug("Validated authentication token for: {}", claims.getBody().getSubject());
            return Optional.ofNullable(claims.getBody().getSubject());
        } catch (Exception e) {
            log.error("Failed to validate JWT token", e);
            return Optional.empty();
        }
    }

}
