package com.samnammae.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.auth_service.config.TestConfig;
import com.samnammae.auth_service.dto.request.LoginRequest;
import com.samnammae.auth_service.dto.request.SignUpRequest;
import com.samnammae.auth_service.dto.response.LoginResponse;
import com.samnammae.auth_service.service.AuthService;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestConfig.class)  // 수동 빈 설정 클래스
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 API 성공 테스트")
    void signupSuccess() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "테스트", "01012345678");
        doNothing().when(authService).signup(any(SignUpRequest.class));

        // when and then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 API 실패 테스트 - 중복 이메일")
    void signupDuplicateEmail() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("test@example.com", "password", "테스트", "01012345678");
        doThrow(new CustomException(ErrorCode.DUPLICATE_EMAIL))
                .when(authService).signup(any(SignUpRequest.class));

        // when and then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 API 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password");
        LoginResponse response = new LoginResponse("access-token", "refresh-token");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        // when and then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 API 실패 테스트 - 존재하지 않는 사용자")
    void loginUserNotFound() throws Exception {
        // given
        LoginRequest request = new LoginRequest("notfound@example.com", "password");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // when and then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 API 실패 테스트 - 비밀번호 불일치")
    void loginInvalidPassword() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_PASSWORD));

        // when and then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그아웃 API 성공 테스트")
    void logoutSuccess() throws Exception {
        // given
        String refreshToken = "valid-refresh-token";
        String authHeader = "Bearer " + refreshToken;

        doNothing().when(authService).logout(refreshToken);

        // when and then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 API 실패 테스트 - Authorization 헤더 없음")
    void logoutMissingHeader() throws Exception {
        // when and then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().is4xxClientError());  // 400 or 401 예상
    }

    @Test
    @DisplayName("로그아웃 API 실패 테스트 - 유효하지 않은 토큰")
    void logoutInvalidToken() throws Exception {
        // given
        String refreshToken = "invalid-refresh-token";
        String authHeader = "Bearer " + refreshToken;

        doThrow(new CustomException(ErrorCode.INVALID_TOKEN))
                .when(authService).logout(refreshToken);

        // when and then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().is4xxClientError());  // 401 Unauthorized 등
    }

}
