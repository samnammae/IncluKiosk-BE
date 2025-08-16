package com.samnammae.auth_service.service;

import com.samnammae.auth_service.client.AdminServiceFeignClient;
import com.samnammae.auth_service.domain.token.RefreshToken;
import com.samnammae.auth_service.domain.token.RefreshTokenRepository;
import com.samnammae.auth_service.domain.user.User;
import com.samnammae.auth_service.domain.user.UserRepository;
import com.samnammae.auth_service.dto.request.LoginRequest;
import com.samnammae.auth_service.dto.request.SignUpRequest;
import com.samnammae.auth_service.dto.response.LoginResponse;
import com.samnammae.auth_service.dto.response.StoreSimpleResponse;
import com.samnammae.auth_service.jwt.JwtUtil;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.common.response.ApiResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AdminServiceFeignClient adminServiceFeignClient;

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

    // DB 조회 분리
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 리프레시 토큰 저장을 별도 분리
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    // 외부 서비스 호출 로직 분리
    public List<Long> getStoreIds(Long userId) {
        List<Long> storeIds = new ArrayList<>();
        try {
            ApiResponse<List<StoreSimpleResponse>> response = adminServiceFeignClient.getStoresByUserId(userId);
            if (response.getCode() == HttpStatus.OK.value()) {
                List<StoreSimpleResponse> stores = response.getData();
                for (StoreSimpleResponse store : stores) {
                    storeIds.add(store.storeId());
                }
            }
        } catch (Exception e) {
            // admin 서비스 호출 실패 시 빈 리스트 반환
            System.out.println("Admin service call failed: " + e.getMessage());
        }
        return storeIds;
    }

    // 로그인 - 각 단계별로 DB 연산 분리
    public LoginResponse login(LoginRequest request) {
        // 1. 사용자 조회 (DB 커넥션 즉시 반환)
        User user = getUserByEmail(request.getEmail());

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. 외부 서비스 호출 (DB와 무관)
        List<Long> storeIds = getStoreIds(user.getId());

        // 4. JWT 토큰 생성
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAtDate = Date.from(now.plusMillis(jwtUtil.getRefreshTokenValidityInMs()));
        LocalDateTime expiresAtLocal = LocalDateTime.ofInstant(expiresAtDate.toInstant(), ZoneId.systemDefault());

        String accessToken = jwtUtil.generateAccessToken(user, storeIds, issuedAt, expiresAtDate);
        String refreshToken = jwtUtil.generateRefreshToken(user, issuedAt, expiresAtDate);

        // 5. 리프레시 토큰 저장 (새로운 트랜잭션으로 빠르게 처리)
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(expiresAtLocal)
                .createdAt(LocalDateTime.now())
                .build();

        saveRefreshToken(refreshTokenEntity);

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
