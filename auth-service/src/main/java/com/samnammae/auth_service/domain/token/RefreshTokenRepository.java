package com.samnammae.auth_service.domain.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 유저 id로 모든 토큰 조회
    List<RefreshToken> findAllByUserId(Long userId);

    // 토큰 string 으로 토큰 조회
    Optional<RefreshToken> findByToken(String token);

    // 유저 id와 토큰 string 으로 토큰 조회
    Optional<RefreshToken> findByUserIdAndToken(Long userId, String token);

    // userId + token 조합으로 삭제
    void deleteByUserIdAndToken(Long userId, String token);
}