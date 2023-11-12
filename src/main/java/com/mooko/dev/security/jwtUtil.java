package com.mooko.dev.security;

import com.mooko.dev.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
@Slf4j
public class jwtUtil{

    public static int getUserId(String token, String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("userId", Integer.class);
    }

    public static boolean isExpired(String token, String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().getExpiration().before(new Date());

    }
    public static String createJwt(User user, String secretKey, Long expiredMs){
        Claims claims = Jwts.claims();
        claims.put("user", user);


        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return jwt;
    }
}