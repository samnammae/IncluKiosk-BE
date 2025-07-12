package com.samnammae.api_gateway.util;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    // 테스트용 시크릿 키 (실제 키와 달라야 하며, 충분히 길어야 함)
    private static final String TEST_SECRET = "this-is-a-test-secret-key-for-jwt-util-class-1234567890";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // 각 테스트가 실행되기 전에 JwtUtil 인스턴스를 생성
        jwtUtil = new JwtUtil(TEST_SECRET);
    }

    @Test
    @DisplayName("성공: 유효한 토큰이 주어지면 클레임을 정상적으로 파싱한다")
    void validateAndParseClaims_Success() {
        // given: 테스트용 유효한 토큰 생성
        String userId = "1";
        String userEmail = "test@example.com";
        String validToken = createTestToken(userId, userEmail, 1000 * 60 * 5); // 5분 뒤 만료

        // when: 토큰 검증 및 파싱 실행
        Claims claims = jwtUtil.validateAndParseClaims(validToken);

        // then: 예외가 발생하지 않고, 클레임 정보가 일치하는지 확인
        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.get("userEmail", String.class)).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("실패: 만료된 토큰이 주어지면 CustomException(EXPIRED_TOKEN)을 던진다")
    void validateAndParseClaims_Fail_ExpiredToken() {
        // given: 만료된 토큰 생성
        String expiredToken = createTestToken("1", "test@example.com", -1000 * 60); // 1분 전 만료

        // when & then: 예외가 발생하는지, 그리고 예외의 ErrorCode가 EXPIRED_TOKEN인지 확인
        assertThatThrownBy(() -> jwtUtil.validateAndParseClaims(expiredToken))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_TOKEN.getMessage());
    }

    @Test
    @DisplayName("실패: 잘못된 서명을 가진 토큰이 주어지면 CustomException(INVALID_TOKEN)을 던진다")
    void validateAndParseClaims_Fail_InvalidSignature() {
        // given: 다른 시크릿 키로 서명된 토큰 생성
        Key wrongKey = Keys.hmacShaKeyFor("this-is-a-completely-different-wrong-secret-key-0987654321".getBytes());
        String invalidSignatureToken = Jwts.builder()
                .setSubject("1")
                .signWith(wrongKey) // 잘못된 키로 서명
                .compact();

        // when & then: 예외가 발생하는지, 그리고 예외의 ErrorCode가 INVALID_TOKEN인지 확인
        assertThatThrownBy(() -> jwtUtil.validateAndParseClaims(invalidSignatureToken))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("실패: 토큰 형식이 잘못된 경우 CustomException(INVALID_TOKEN)을 던진다")
    void validateAndParseClaims_Fail_MalformedToken() {
        // given: 형식이 잘못된 문자열
        String malformedToken = "this.is.not.a.jwt";

        // when & then: 예외가 발생하는지, 그리고 예외의 ErrorCode가 INVALID_TOKEN인지 확인
        assertThatThrownBy(() -> jwtUtil.validateAndParseClaims(malformedToken))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("실패: 토큰이 null인 경우 CustomException(TOKEN_MISSING)을 던진다")
    void validateAndParseClaims_Fail_NullToken() {
        // given: null 토큰
        String nullToken = null;

        // when & then: 예외가 발생하는지, 그리고 예외의 ErrorCode가 TOKEN_MISSING인지 확인
        assertThatThrownBy(() -> jwtUtil.validateAndParseClaims(null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TOKEN_MISSING.getMessage());
    }


    /**
     * 테스트용 JWT를 생성하는 헬퍼 메서드
     *
     * @param userId    사용자 ID
     * @param userEmail 사용자 이메일
     * @param validityInMs 토큰 유효 시간 (밀리초)
     * @return 생성된 JWT 문자열
     */
    private String createTestToken(String userId, String userEmail, long validityInMs) {
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(userId)
                .claim("userEmail", userEmail)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
