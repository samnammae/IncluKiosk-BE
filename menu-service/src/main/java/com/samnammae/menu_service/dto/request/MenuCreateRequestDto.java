package com.samnammae.menu_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequestDto {
    private String name;
    private int price;
    private String description;
    private Long categoryId;
    private String optionCategoryIds;
    private boolean isSoldOut = false;
}
