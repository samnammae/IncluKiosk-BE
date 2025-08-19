package com.samnammae.menu_service.dto.response;

import com.samnammae.menu_service.domain.menu.Menu;
import com.samnammae.menu_service.domain.option.Option;
import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class MenuDetailResponseDto {
    private Long menuId;
    private String menuName;
    private int basePrice;
    private String description;
    private String imageUrl;
    private boolean isSoldOut;
    private List<OptionCategoryDto> optionCategories; // 카테고리별로 그룹핑

    @Getter
    @Builder
    public static class OptionCategoryDto {
        private Long categoryId;
        private String categoryName;
        private List<OptionDto> options;
    }

    @Getter
    @Builder
    public static class OptionDto {
        private Long optionId;
        private String optionName;
        private int price;
    }

    public static MenuDetailResponseDto from(Menu menu) {
        Map<Long, List<Option>> optionsByCategory = menu.getOptionCategories().stream()
                .collect(Collectors.toMap(
                        OptionCategory::getId,
                        OptionCategory::getOptions
                ));

        List<OptionCategoryDto> optionCategories = menu.getOptionCategories().stream()
                .map(category -> OptionCategoryDto.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .options(category.getOptions().stream()
                                .map(option -> OptionDto.builder()
                                        .optionId(option.getId())
                                        .optionName(option.getName())
                                        .price(option.getPrice())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return MenuDetailResponseDto.builder()
                .menuId(menu.getId())
                .menuName(menu.getName())
                .basePrice(menu.getPrice())
                .description(menu.getDescription())
                .imageUrl(menu.getImageUrl())
                .isSoldOut(menu.isSoldOut())
                .optionCategories(optionCategories)
                .build();
    }
}