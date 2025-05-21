package com.samnammae.auth_service.service;

import com.samnammae.auth_service.domain.token.RefreshToken;
import com.samnammae.auth_service.domain.token.RefreshTokenRepository;
import com.samnammae.auth_service.domain.user.User;
import com.samnammae.auth_service.domain.user.UserRepository;
import com.samnammae.auth_service.dto.request.LoginRequest;
import com.samnammae.auth_service.dto.request.SignUpRequest;
import com.samnammae.auth_service.dto.response.LoginResponse;
import com.samnammae.auth_service.jwt.JwtUtil;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    public void signup(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        // 저장
        userRepository.save(user);
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAtDate = Date.from(now.plusMillis(jwtUtil.getRefreshTokenValidityInMs()));
        LocalDateTime expiresAtLocal = LocalDateTime.ofInstant(expiresAtDate.toInstant(), ZoneId.systemDefault());

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user, issuedAt, expiresAtDate);
        String refreshToken = jwtUtil.generateRefreshToken(user, issuedAt, expiresAtDate);

        // 기존 토큰 있으면 업데이트, 없으면 새로 저장
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserIdAndToken(user.getId(), refreshToken);

        if (existingToken.isPresent()) {
            existingToken.get().updateToken(refreshToken, expiresAtLocal);
        } else {
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .userId(user.getId())
                            .token(refreshToken)
                            .expiresAt(expiresAtLocal)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }

        return new LoginResponse(accessToken, refreshToken);
    }

    // 로그아웃
    @Transactional
    public void logout(String refreshToken) {
        // 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 userId 추출
        Claims claims = jwtUtil.getClaims(refreshToken);
        Long userId = Long.valueOf(claims.get("sub").toString());

        // DB에서 해당 토큰만 삭제
        refreshTokenRepository.deleteByUserIdAndToken(userId, refreshToken);
    }

}
