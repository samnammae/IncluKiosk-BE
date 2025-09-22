package com.samnammae.chatbot_service.client;

import com.samnammae.chatbot_service.dto.request.OrderRequestDto;
import com.samnammae.chatbot_service.dto.response.OrderResponseDto;
import com.samnammae.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @PostMapping("/api/order")
    ApiResponse<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto request);
}