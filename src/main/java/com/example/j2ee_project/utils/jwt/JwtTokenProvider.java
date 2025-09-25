package com.example.j2ee_project.utils.jwt;

import com.example.j2ee_project.config.JwtConfig;
import com.example.j2ee_project.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long tokenExpirationMs;
    @SuppressWarnings("unused")
    private final UserRepository userRepository;

    public JwtTokenProvider(JwtConfig jwtConfig, UserRepository userRepository) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getPrivateKey()));
        this.tokenExpirationMs = jwtConfig.getTokenExpirationMs();
        this.userRepository = userRepository;
    }

    public String generateToken(String data, long expiration) {
        return Jwts.builder()
                .subject(data)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(String data) {
        return generateToken(data, tokenExpirationMs);
    }

    public boolean verifyToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.parseInt(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject(); // Trả về username thay vì parse thành Integer
    }
}
