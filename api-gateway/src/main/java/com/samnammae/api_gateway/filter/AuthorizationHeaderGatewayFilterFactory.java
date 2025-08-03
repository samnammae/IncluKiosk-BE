package com.samnammae.api_gateway.filter;

import com.samnammae.api_gateway.util.JwtUtil;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class AuthorizationHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthorizationHeaderGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public AuthorizationHeaderGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        // 필터에 필요한 설정이 있다면 여기에 정의합니다.
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 'Authorization' 헤더 존재 여부 확인
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                // 에러 발생 시, 스트림에 에러 신호를 보냄 (글로벌 핸들러가 처리)
                return Mono.error(new CustomException(ErrorCode.TOKEN_MISSING));
            }

            String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            String token = authorizationHeader.replace("Bearer ", "");

            // 토큰 유효성 검증 및 클레임 추출
            try {
                Claims claims = jwtUtil.validateAndParseClaims(token);
                String userId = claims.getSubject();
                String userEmail = claims.get("userEmail", String.class);
                String storeIds = claims.get("storeIds", String.class);

                ServerHttpRequest newRequest = request.mutate()
                        .header("X-USER-ID", userId)
                        .header("X-USER-EMAIL", userEmail)
                        .header("X-MANAGED-STORE-IDS", storeIds)
                        .build();

                return chain.filter(exchange.mutate().request(newRequest).build());

            } catch (CustomException e) {
                // 이 신호는 GatewayExceptionHandler가 처리합니다.
                return Mono.error(e);
            }
        };
    }
}
