package com.samnammae.auth_service.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회 테스트")
    void findByEmail() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encrypted")
                .name("테스트")
                .phone("01012345678")
                .build();
        userRepository.save(user);

        // when
        User result = userRepository.findByEmail("test@example.com").orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일 중복 여부 확인 테스트")
    void existsByEmail() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encrypted")
                .name("테스트")
                .phone("01012345678")
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }
}