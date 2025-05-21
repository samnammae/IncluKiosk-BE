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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() {
        // given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "테스트", "01012345678");
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

        // when
        authService.signup(request);

        // then
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복 이메일")
    void signupDuplicateEmail() {
        // given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "테스트", "01012345678");
        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when and then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    org.assertj.core.api.Assertions.assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
                });
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password");

        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .name("테스트")
                .phone("01012345678")
                .build();

        long refreshValidityMs = 600000L; // 10분
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.getRefreshTokenValidityInMs()).willReturn(refreshValidityMs);
        given(jwtUtil.generateAccessToken(eq(user), any(Date.class), any(Date.class))).willReturn(accessToken);
        given(jwtUtil.generateRefreshToken(eq(user), any(Date.class), any(Date.class))).willReturn(refreshToken);
        given(refreshTokenRepository.findByUserIdAndToken(user.getId(), refreshToken)).willReturn(Optional.empty());

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        then(refreshTokenRepository).should(times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트 - 기존 리프레시 토큰 업데이트")
    void loginSuccessWithExistingRefreshTokenUpdate() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password");

        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .name("테스트")
                .phone("01012345678")
                .build();

        long refreshValidityMs = 600000L;
        String accessToken = "access-token";
        String refreshToken = "existing-refresh-token";

        // 기존 리프레시 토큰 객체 (spy로 감시)
        RefreshToken existingToken = spy(RefreshToken.builder()
                .id(99L)
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build());

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.getRefreshTokenValidityInMs()).willReturn(refreshValidityMs);
        given(jwtUtil.generateAccessToken(eq(user), any(Date.class), any(Date.class))).willReturn(accessToken);
        given(jwtUtil.generateRefreshToken(eq(user), any(Date.class), any(Date.class))).willReturn(refreshToken);
        given(refreshTokenRepository.findByUserIdAndToken(user.getId(), refreshToken)).willReturn(Optional.of(existingToken));

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);

        // updateToken()이 호출되었는지 확인
        verify(existingToken, times(1)).updateToken(eq(refreshToken), any(LocalDateTime.class));

        // 새로 저장하지 않았는지도 확인
        then(refreshTokenRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 존재하지 않는 이메일")
    void loginUserNotFound() {
        // given
        LoginRequest request = new LoginRequest("notfound@example.com", "password");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 비밀번호 불일치")
    void loginInvalidPassword() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");

        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .name("테스트")
                .phone("01012345678")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when and then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
                });
    }

    @Test
    @DisplayName("로그아웃 성공 테스트 - 유효한 Refresh Token 삭제")
    void logoutSuccess() {
        // given
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;

        Claims claims = mock(Claims.class);
        given(jwtUtil.validateToken(refreshToken)).willReturn(true);
        given(jwtUtil.getClaims(refreshToken)).willReturn(claims);
        given(claims.get("sub")).willReturn(String.valueOf(userId));

        // when
        authService.logout(refreshToken);

        // then
        then(refreshTokenRepository).should().deleteByUserIdAndToken(userId, refreshToken);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - 유효하지 않은 토큰")
    void logoutFailInvalidToken() {
        // given
        String invalidToken = "invalid-refresh-token";
        given(jwtUtil.validateToken(invalidToken)).willReturn(false);

        // when and then
        assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                });

        // 삭제가 시도되지 않았는지 확인
        then(refreshTokenRepository).shouldHaveNoInteractions();
    }

}