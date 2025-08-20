package com.samnammae.order_service.domain.order;

import com.samnammae.order_service.domain.orderitem.OrderItem;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Document(collection = "orders")
public class Order {

    @Id // 이 필드가 MongoDB의 '_id' 필드와 매핑됨
    private String id;

    @Field("order_number") // 실제 DB 필드명 지정 (선택사항)
    private String orderNumber;

    @Field("store_id")
    private Long storeId;

    @Field("store_name")
    private String storeName;

    @Field("order_type")
    private String orderType; // "STORE", "TAKEOUT"

    @Field("payment_method")
    private String paymentMethod; // "CARD", "CASH"

    @Field("status")
    private String status; // "READY", "CANCELLED"

    @Field("items")
    private List<OrderItem> items; // 주문 아이템 목록을 내장

    @Field("total_amount")
    private int totalAmount; // 총 주문 금액

    @Field("total_items")
    private int totalItems; // 총 아이템 수량

    @CreatedDate // 문서 생성 시 날짜/시간 자동 기록
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate // 문서 수정 시 날짜/시간 자동 기록
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public Order cancel() {
        return Order.builder()
                .id(this.id)
                .orderNumber(this.orderNumber)
                .storeId(this.storeId)
                .storeName(this.storeName)
                .orderType(this.orderType)
                .paymentMethod(this.paymentMethod)
                .status("CANCELLED")
                .items(this.items)
                .totalAmount(this.totalAmount)
                .totalItems(this.totalItems)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
