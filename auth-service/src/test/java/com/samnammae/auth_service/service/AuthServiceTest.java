package com.samnammae.auth_service.service;

import com.samnammae.auth_service.domain.user.User;
import com.samnammae.auth_service.domain.user.UserRepository;
import com.samnammae.auth_service.dto.request.SignUpRequest;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;    
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

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

}