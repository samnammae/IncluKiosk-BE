package com.samnammae.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final Key secretKey;

    public JwtUtil(
            @Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            // 토큰을 파싱하면서 유효성 검증 (서명, 만료 시간 등)
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 유효하지 않은 토큰 (서명 오류, 만료 등)일 경우 예외 발생
            return false;
        }
    }

    // 토큰 클레임 추출
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
