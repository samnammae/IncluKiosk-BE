package com.samnammae.order_service.controller;

import com.samnammae.common.response.ApiResponse;
import com.samnammae.order_service.dto.request.OrderCreateRequestDto;
import com.samnammae.order_service.dto.response.OrderCancelResponseDto;
import com.samnammae.order_service.dto.response.OrderCreateResponseDto;
import com.samnammae.order_service.dto.response.OrderDetailResponseDto;
import com.samnammae.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@Tag(name = "Order", description = "주문 관련 API")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    public ApiResponse<OrderCreateResponseDto> createOrder(@RequestBody OrderCreateRequestDto requestDto) {
        OrderCreateResponseDto response = orderService.createOrder(requestDto);
        return ApiResponse.success(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문의 상세 정보를 조회합니다.")
    public ApiResponse<OrderDetailResponseDto> getOrder(@PathVariable String orderId) {
        OrderDetailResponseDto response = orderService.getOrderById(orderId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{orderId}")
    @Operation(summary = "주문 취소", description = "주문 ID로 주문을 취소합니다.")
    public ApiResponse<OrderCancelResponseDto> cancelOrder(@PathVariable String orderId) {
        OrderCancelResponseDto response = orderService.cancelOrder(orderId);
        return ApiResponse.success(response);
    }
}