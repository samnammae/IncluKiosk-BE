package com.samnammae.chatbot_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    private Long storeId;
    private String storeName;
    private String orderType;
    private String paymentMethod;
    private List<OrderItemDto> items;
    private int totalAmount;
    private int totalItems;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Long menuId;
        private String menuName;
        private int basePrice;
        private Map<Long, List<Long>> selectedOptions;
        private int optionPrice;
        private int quantity;
        private int totalPrice;
    }
}