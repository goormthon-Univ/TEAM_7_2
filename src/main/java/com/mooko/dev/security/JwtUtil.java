package com.mooko.dev.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    public int getUserId(String token){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("userId", Integer.class);
    }

    public boolean isExpired(String token){
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                    .getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
            log.error("JWT Parsing error", e);
            return true; // 또는 적절한 예외 처리
        }
    }

    public String createJwt(Long userId, Long expiredMs){
        return createToken(userId, expiredMs);
    }

    public String createRefreshJwt(Long userId, Long expiredMs) {
        return createToken(userId, expiredMs);
    }

    private String createToken(Long userId, Long expiredMs) {
        Claims claims = Jwts.claims().put("userId", userId);
        Instant now = Instant.now();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expiredMs)))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}