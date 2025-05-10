package com.samnammae.auth_service.domain.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("userId로 리프레시 토큰 전체 조회 테스트")
    void findAllByUserId() {
        // given
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(1L)
                .token("refresh-token-1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(1L)
                .token("refresh-token-2")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build());

        // when
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(1L);

        // then
        assertThat(tokens).hasSize(2);
        assertThat(tokens).extracting("token").contains("refresh-token-1", "refresh-token-2");
    }

    @Test
    @DisplayName("토큰 문자열로 리프레시 토큰 조회 테스트")
    void findByToken() {
        // given
        RefreshToken token = RefreshToken.builder()
                .userId(2L)
                .token("refresh-token-456")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(token);

        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("refresh-token-456");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("userId와 token으로 리프레시 토큰 조회 테스트")
    void findByUserIdAndToken() {
        // given
        Long userId = 4L;
        String tokenStr = "refresh-token-999";

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(token);

        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByUserIdAndToken(userId, tokenStr);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getToken()).isEqualTo(tokenStr);
    }

    @Test
    @DisplayName("userId와 token으로 리프레시 토큰 삭제 테스트")
    void deleteByUserIdAndToken() {
        // given
        String tokenStr = "refresh-token-789";
        Long userId = 3L;

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build());

        // when
        refreshTokenRepository.deleteByUserIdAndToken(userId, tokenStr);

        // then
        Optional<RefreshToken> result = refreshTokenRepository.findByUserIdAndToken(userId, tokenStr);
        assertThat(result).isNotPresent();
    }

}