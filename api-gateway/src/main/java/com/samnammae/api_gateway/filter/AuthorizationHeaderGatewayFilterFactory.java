package com.samnammae.api_gateway.filter;

import com.samnammae.api_gateway.util.JwtUtil;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class AuthorizationHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthorizationHeaderGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationHeaderGatewayFilterFactory.class);

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
            String requestPath = request.getPath().value();

            // 'Authorization' 헤더 존재 여부 확인
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn("Authorization header missing - Path: {}, RemoteAddress: {}",
                        requestPath, request.getRemoteAddress());
                return Mono.error(new CustomException(ErrorCode.TOKEN_MISSING));
            }

            String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            String token = authorizationHeader.replace("Bearer ", "");

            // 토큰 유효성 검증 및 클레임 추출
            try {
                Claims claims = jwtUtil.validateAndParseClaims(token);
                String userId = claims.getSubject();
                String userEmail = claims.get("userEmail", String.class);
                Object storeIdsObj = claims.get("storeIds");
                List<Long> storeIdsList = null;
                if (storeIdsObj instanceof List<?>) {
                    storeIdsList = ((List<?>) storeIdsObj).stream()
                            .filter(item -> item instanceof Number)
                            .map(item -> ((Number) item).longValue())
                            .toList();
                }
                String storeIds = storeIdsList != null ?
                        storeIdsList.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) : "";

                logger.debug("JWT validation successful - Path: {}, UserId: {}, UserEmail: {}",
                        requestPath, userId, userEmail);

                ServerHttpRequest newRequest = request.mutate()
                        .header("X-USER-ID", userId)
                        .header("X-USER-EMAIL", userEmail)
                        .header("X-MANAGED-STORE-IDS", storeIds)
                        .build();

                return chain.filter(exchange.mutate().request(newRequest).build());

            } catch (CustomException e) {
                logger.warn("JWT validation failed - Path: {}, ErrorCode: {}, RemoteAddress: {}",
                        requestPath, e.getErrorCode().name(), request.getRemoteAddress());
                return Mono.error(e);
            }
        };
    }
}