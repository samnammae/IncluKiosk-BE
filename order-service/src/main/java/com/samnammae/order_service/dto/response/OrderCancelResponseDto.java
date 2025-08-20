package com.samnammae.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCancelResponseDto {
    private String orderId;
    private int refundAmount;
}