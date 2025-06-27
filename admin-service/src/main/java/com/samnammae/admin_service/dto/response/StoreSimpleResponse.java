package com.samnammae.admin_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.samnammae.admin_service.domain.store.Store;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class StoreSimpleResponse {

    private Long storeId;

    private String name;
    private String phone;
    private String address;

    private String mainImg;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    @Builder
    private StoreSimpleResponse(Long storeId, String name, String phone, String address, String mainImg, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.storeId = storeId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.mainImg = mainImg;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * @param store 변환할 Store 엔티티 객체
     * @return 변환된 StoreSimpleResponse DTO 객체
     */
    public static StoreSimpleResponse from(Store store) {
        return StoreSimpleResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .phone(store.getPhone())
                .address(store.getAddress())
                .mainImg(store.getMainImgUrl())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
