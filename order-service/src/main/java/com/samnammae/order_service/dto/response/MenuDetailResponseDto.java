package com.samnammae.order_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDetailResponseDto {

    private Long menuId;
    private String menuName;
    private int basePrice;
    private String description;
    private String imageUrl;

    @JsonProperty("soldOut")
    private boolean soldOut;

    private List<OptionCategoryDto> optionCategories; // 카테고리별로 그룹핑

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionCategoryDto {
        private Long categoryId;
        private String categoryName;
        private List<OptionDto> options;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDto {
        private Long optionId;
        private String optionName;
        private int price;
    }
}