package com.outdoor.gear.security;

import com.outdoor.gear.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return generateToken(username, roles);
    }

    public String generateToken(String username, List<String> roles) {
        return generateToken(username, String.join(",", roles));
    }

    public String generateToken(String username, String roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object roles = getClaims(token).get("roles");
        if (roles instanceof String s && !s.isEmpty()) {
            return Arrays.asList(s.split(","));
        }
        return List.of();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }
}
