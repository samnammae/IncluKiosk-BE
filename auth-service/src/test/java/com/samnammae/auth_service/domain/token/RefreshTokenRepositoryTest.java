package com.samnammae.auth_service.domain.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("userId로 리프레시 토큰 조회 테스트")
    void findByUserId() {
        // given
        RefreshToken token = RefreshToken.builder()
                .userId(1L)
                .token("refresh-token-123")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(token);

        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByUserId(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("refresh-token-123");
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
    @DisplayName("userId로 리프레시 토큰 삭제 테스트")
    void deleteByUserId() {
        // given
        RefreshToken token = RefreshToken.builder()
                .userId(3L)
                .token("refresh-token-789")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(token);

        // when
        refreshTokenRepository.deleteByUserId(3L);

        // then
        Optional<RefreshToken> result = refreshTokenRepository.findByUserId(3L);
        assertThat(result).isNotPresent();
    }
}