package com.social_media_app.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService {
    private final byte[] key;
    private final long ttlMin;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.ttl-min}") long ttlMin) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlMin = ttlMin;
    }

    public String generate(String subject) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttlMin, ChronoUnit.MINUTES)))
                .signWith(Keys.hmacShaKeyFor(key), Jwts.SIG.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(key))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

