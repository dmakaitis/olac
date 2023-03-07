package org.olac.reservation.utility.spring;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.config.OlacProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtility {

    private final OlacProperties properties;

    private Key getKey() {
        return Keys.hmacShaKeyFor((properties.getJwt().getKey() + properties.getJwt().getKey()).getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + TimeUnit.MINUTES.toMillis(properties.getJwt().getTimeoutMinutes())))
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
