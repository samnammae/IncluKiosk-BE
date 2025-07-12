package com.samnammae.api_gateway.util;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
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

    public Claims validateAndParseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 토큰 만료 시
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (SignatureException | MalformedJwtException e) {
            // 서명 오류 또는 토큰 형식이 잘못된 경우
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            // 토큰이 null이거나 비어있는 경우
            throw new CustomException(ErrorCode.TOKEN_MISSING);
        }
    }
}
