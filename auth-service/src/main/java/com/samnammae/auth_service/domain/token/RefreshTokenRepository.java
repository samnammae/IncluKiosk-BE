package com.samnammae.auth_service.domain.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 유저 id로 토큰 조회
    Optional<RefreshToken> findByUserId(Long userId);

    // 토큰 string 으로 토큰 조회
    Optional<RefreshToken> findByToken(String token);

    // 유저 id로 토큰 삭제
    void deleteByUserId(Long userId);
}