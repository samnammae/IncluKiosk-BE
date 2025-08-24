package com.samnammae.menu_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionRequestDto {
    private String name;
    private int price;

    @JsonProperty("default")
    private boolean isDefault;
}