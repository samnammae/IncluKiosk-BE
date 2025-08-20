package com.samnammae.order_service.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class OrderCreateRequestDto {

    private Long storeId;
    private String storeName;
    private String orderType;
    private String paymentMethod;
    private List<OrderItemDto> items;
    private int totalAmount;
    private int totalItems;

    // items 리스트 안의 객체 구조
    @Getter
    @NoArgsConstructor
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