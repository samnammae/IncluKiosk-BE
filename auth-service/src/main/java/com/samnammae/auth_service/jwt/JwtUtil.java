package com.samnammae.auth_service.jwt;

import com.samnammae.auth_service.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;
    @Getter
    private final long accessTokenValidityInMs;
    @Getter
    private final long refreshTokenValidityInMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidityInMs,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidityInMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }

    // 엑세스 토큰 발행
    public String generateAccessToken(User user, Date issuedAt, Date expiresAt) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(String.valueOf(user.getId()))
                .claim("userEmail", user.getEmail())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    // 리프레쉬 토큰 발행
    public String generateRefreshToken(User user, Date issuedAt, Date expiresAt) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 로그 찍거나 예외 처리 로직 추가 가능
            return false;
        }
    }

    // 클레임 추출
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}