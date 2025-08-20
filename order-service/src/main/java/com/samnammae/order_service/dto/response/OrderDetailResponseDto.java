package com.samnammae.order_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class OrderDetailResponseDto {

    private String orderId;
    private String orderNumber;
    private Long storeId;
    private String storeName; // 메뉴 서비스에서 조회해온 매장 이름
    private String orderType;
    private String paymentMethod;
    private String status;
    private List<OrderItemDto> items;
    private int totalAmount;
    private int totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class OrderItemDto {
        private Long menuId;
        private String menuName;
        private int basePrice;
        private Map<String, List<String>> selectedOptions;
        private int optionPrice;
        private int quantity;
        private int totalPrice;
    }
}
