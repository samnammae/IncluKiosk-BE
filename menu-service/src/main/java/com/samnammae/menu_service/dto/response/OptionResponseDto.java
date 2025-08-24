package com.samnammae.menu_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.samnammae.menu_service.domain.option.Option;
import lombok.Getter;

@Getter
public class OptionResponseDto {
    private final Long id;
    private final String name;
    private final int price;

    @JsonProperty("default")
    private final boolean isDefault;

    public OptionResponseDto(Option option) {
        this.id = option.getId();
        this.name = option.getName();
        this.price = option.getPrice();
        this.isDefault = option.isDefault();
    }
}