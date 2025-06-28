package com.samnammae.admin_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoreResponse {

    private String storeId;
    private String name;
    private String phone;
    private String address;
    private String mainImg;

    private StartPage startPage;

    private Theme theme;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    @Builder
    public StoreResponse(String storeId, String name, String phone, String address,
                         String mainImg, StartPage startPage, Theme theme,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.storeId = storeId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.mainImg = mainImg;
        this.startPage = startPage;
        this.theme = theme;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * @param store 변환할 Store 엔티티 객체
     * @return 변환된 StoreResponse DTO 객체
     */
    public static StoreResponse from(com.samnammae.admin_service.domain.store.Store store) {
        return StoreResponse.builder()
                .storeId(store.getId().toString())
                .name(store.getName())
                .phone(store.getPhone())
                .address(store.getAddress())
                .mainImg(store.getMainImgUrl())
                .startPage(new StartPage(store.getLogoImgUrl(), store.getIntroduction(), store.getStartBackgroundUrl()))
                .theme(new Theme(store.getMainColor(), store.getSubColor(), store.getTextColor()))
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    @Getter
    public static class StartPage {
        private final String logoImg;
        private final String introduction;
        private final String startBackground;

        @Builder
        public StartPage(String logoImg, String introduction, String startBackground) {
            this.logoImg = logoImg;
            this.introduction = introduction;
            this.startBackground = startBackground;
        }
    }

    @Getter
    public static class Theme {
        private final String mainColor;
        private final String subColor;
        private final String textColor;

        @Builder
        public Theme(String mainColor, String subColor, String textColor) {
            this.mainColor = mainColor;
            this.subColor = subColor;
            this.textColor = textColor;
        }
    }
}
