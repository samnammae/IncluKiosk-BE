package com.samnammae.api_gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorCode errorCode;
        if (ex instanceof CustomException) {
            CustomException customEx = (CustomException) ex;
            errorCode = customEx.getErrorCode();

            // CustomException의 경우 WARN 레벨로 로깅
            logger.warn("Gateway Custom Exception - Code: {}, Message: {}, Path: {}",
                    errorCode.name(), errorCode.getMessage(),
                    exchange.getRequest().getPath().value(), ex);
        } else {
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

            // 예상치 못한 예외의 경우 ERROR 레벨로 로깅
            logger.error("Gateway Unexpected Exception - Path: {}, Exception: {}",
                    exchange.getRequest().getPath().value(), ex.getClass().getSimpleName(), ex);
        }

        response.setStatusCode(HttpStatus.valueOf(errorCode.getStatus()));
        ApiResponse<Object> apiResponse = ApiResponse.error(errorCode.getStatus(), errorCode.getMessage());

        try {
            byte[] errorBytes = objectMapper.writeValueAsBytes(apiResponse);
            DataBuffer buffer = response.bufferFactory().wrap(errorBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            // JSON 변환 실패 시 ERROR 레벨로 로깅
            logger.error("Failed to serialize error response to JSON - Path: {}",
                    exchange.getRequest().getPath().value(), e);

            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }
}