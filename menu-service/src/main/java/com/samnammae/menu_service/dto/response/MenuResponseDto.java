package com.samnammae.menu_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samnammae.menu_service.domain.menu.Menu;
import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDto {
    private Long id;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
    private List<Long> optionCategoryIds;

    @JsonProperty("isSoldOut")
    private boolean isSoldOut;

    @JsonIgnore
    public boolean isSoldOut() {
        return this.isSoldOut;
    }

    public static MenuResponseDto from(Menu menu) {
        return new MenuResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.getOptionCategories().stream()
                        .map(OptionCategory::getId)
                        .toList(),
                menu.isSoldOut()
        );
    }
}
