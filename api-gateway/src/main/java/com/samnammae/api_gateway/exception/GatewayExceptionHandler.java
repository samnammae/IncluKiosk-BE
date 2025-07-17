package com.samnammae.api_gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.common.response.ApiResponse;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1) // 다른 스프링 기본 핸들러보다 우선순위를 높게 설정하여 먼저 실행되도록 함
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 이미 응답이 커밋된 경우, 더 이상 처리하지 않음
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 응답 헤더 설정
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorCode errorCode;
        // 우리가 직접 정의한 CustomException인 경우, 해당 ErrorCode를 사용
        if (ex instanceof CustomException) {
            errorCode = ((CustomException) ex).getErrorCode();
        } else {
            // 그 외 처리하지 않은 예외는 일반적인 서버 에러로 처리 (필요에 따라 확장 가능)
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        }

        response.setStatusCode(HttpStatus.valueOf(errorCode.getStatus()));
        ApiResponse<Object> apiResponse = ApiResponse.error(errorCode.getStatus(), errorCode.getMessage());

        try {
            // ApiResponse 객체를 JSON 바이트로 변환
            byte[] errorBytes = objectMapper.writeValueAsBytes(apiResponse);
            DataBuffer buffer = response.bufferFactory().wrap(errorBytes);
            // 응답 스트림에 에러 데이터 작성
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            // JSON 변환 실패 시, 내부 서버 에러로 응답
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }
}
