package com.samnammae.chatbot_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private String orderId;
    private String orderNumber;
}