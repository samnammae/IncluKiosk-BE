package com.samnammae.api_gateway.filter;

import com.samnammae.api_gateway.util.JwtUtil;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthorizationHeaderFilter에 대한 단위 테스트 클래스입니다.
 * 외부 의존성(JwtUtil)을 Mocking하여 필터의 로직만을 독립적으로 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationHeaderGatewayFilterFactoryTest {

    // @Mock: Mockito가 Mock 객체를 생성합니다.
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    // @InjectMocks: @Mock으로 생성된 Mock 객체들을 테스트 대상 클래스에 주입합니다.
    @InjectMocks
    private AuthorizationHeaderGatewayFilterFactory authorizationHeaderFilter;

    private AuthorizationHeaderGatewayFilterFactory.Config config;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 필터 Config 객체를 생성합니다.
        config = new AuthorizationHeaderGatewayFilterFactory.Config();
        // 각 테스트에 필요한 스터빙은 해당 테스트 메소드 내부에서 개별적으로 설정합니다.
    }

    @Test
    @DisplayName("성공: 유효한 JWT 토큰이 헤더에 포함된 경우")
    void apply_withValidToken_shouldAddHeadersAndProceed() {
        // given: 테스트 준비
        String token = "valid-jwt-token";
        String userId = "testUser";
        String userEmail = "test@example.com";

        // MockServerHttpRequest: 테스트용 가짜 HTTP 요청 객체 생성
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header("Authorization", "Bearer " + token)
                .build();

        // MockServerWebExchange: 가짜 요청을 포함하는 교환(exchange) 객체 생성
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Claims: JWT 토큰에서 추출될 정보를 담는 Mock 객체 생성
        Claims claims = new DefaultClaims();
        claims.setSubject(userId);
        claims.put("userEmail", userEmail);

        // jwtUtil.validateAndParseClaims 메소드가 호출되면, 준비된 claims 객체를 반환하도록 설정
        when(jwtUtil.validateAndParseClaims(token)).thenReturn(claims);
        // 이 테스트는 filterChain.filter()가 호출되므로, 해당 호출에 대한 스터빙을 설정합니다.
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // when: 테스트 대상 메소드 실행
        Mono<Void> result = authorizationHeaderFilter.apply(config).filter(exchange, filterChain);

        // then: 결과 검증
        StepVerifier.create(result)
                .verifyComplete(); // Mono가 성공적으로 완료되는지 검증

        // filterChain.filter가 정확히 1번 호출되었는지 검증
        verify(filterChain, times(1)).filter(argThat(ex -> {
            // 다음 필터로 넘어가는 요청(request)에 X-USER-ID와 X-USER-EMAIL 헤더가 올바르게 추가되었는지 검증
            String addedUserId = ex.getRequest().getHeaders().getFirst("X-USER-ID");
            String addedUserEmail = ex.getRequest().getHeaders().getFirst("X-USER-EMAIL");
            return userId.equals(addedUserId) && userEmail.equals(addedUserEmail);
        }));
    }

    @Test
    @DisplayName("실패: Authorization 헤더가 없는 경우")
    void apply_withMissingAuthorizationHeader_shouldThrowException() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // when
        Mono<Void> result = authorizationHeaderFilter.apply(config).filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                // Mono가 에러를 방출하고, 그 에러가 CustomException이며 ErrorCode가 TOKEN_MISSING인지 검증
                .expectErrorMatches(throwable ->
                        throwable instanceof CustomException &&
                                ((CustomException) throwable).getErrorCode() == ErrorCode.TOKEN_MISSING)
                .verify();

        // filterChain.filter가 호출되지 않았는지 검증
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("실패: JWT 토큰 유효성 검증에 실패하는 경우")
    void apply_withInvalidToken_shouldThrowException() {
        // given
        String invalidToken = "invalid-jwt-token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header("Authorization", "Bearer " + invalidToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // jwtUtil이 유효하지 않은 토큰으로 CustomException을 발생시키도록 설정
        when(jwtUtil.validateAndParseClaims(anyString()))
                .thenThrow(new CustomException(ErrorCode.INVALID_TOKEN));

        // when
        Mono<Void> result = authorizationHeaderFilter.apply(config).filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                // Mono가 에러를 방출하고, 그 에러가 CustomException이며 ErrorCode가 TOKEN_INVALID인지 검증
                .expectErrorMatches(throwable ->
                        throwable instanceof CustomException &&
                                ((CustomException) throwable).getErrorCode() == ErrorCode.INVALID_TOKEN)
                .verify();

        // filterChain.filter가 호출되지 않았는지 검증
        verify(filterChain, never()).filter(any());
    }
}
