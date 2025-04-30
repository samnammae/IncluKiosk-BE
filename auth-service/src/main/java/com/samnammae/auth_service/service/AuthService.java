package com.samnammae.auth_service.service;

import com.samnammae.auth_service.domain.User;
import com.samnammae.auth_service.domain.UserRepository;
import com.samnammae.auth_service.dto.request.SignUpRequest;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
}
