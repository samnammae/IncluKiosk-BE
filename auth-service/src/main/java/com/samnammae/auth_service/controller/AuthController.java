package com.samnammae.auth_service.controller;

import com.samnammae.auth_service.dto.request.LoginRequest;
import com.samnammae.auth_service.dto.request.SignUpRequest;
import com.samnammae.auth_service.dto.response.LoginResponse;
import com.samnammae.auth_service.service.AuthService;
import com.samnammae.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름, 전화번호를 입력해 회원가입")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignUpRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력해 로그인하고 JWT 토큰을 발급")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
