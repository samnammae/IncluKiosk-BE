package com.samnammae.menu_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequestDto {
    private String name;
    private int price;
    private String description;
    private Long categoryId;
    private String optionCategoryIds;

    @JsonProperty("isSoldOut")
    private boolean isSoldOut;
}
