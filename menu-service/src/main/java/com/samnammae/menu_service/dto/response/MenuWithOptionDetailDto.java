package com.samnammae.menu_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.samnammae.menu_service.domain.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuWithOptionDetailDto {
    private Long id;
    private String name;
    private int price;
    private String description;
    private List<OptionCategoryResponseDto> optionCategories;

    @JsonProperty("isSoldOut")
    private boolean soldOut;

    public static MenuWithOptionDetailDto from(Menu menu) {
        return new MenuWithOptionDetailDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getOptionCategories().stream()
                        .map(OptionCategoryResponseDto::new)
                        .toList(),
                menu.isSoldOut()
        );
    }
}