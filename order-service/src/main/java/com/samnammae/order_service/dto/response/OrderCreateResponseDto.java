package com.samnammae.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreateResponseDto {
    private String orderId;
    private String orderNumber;
}