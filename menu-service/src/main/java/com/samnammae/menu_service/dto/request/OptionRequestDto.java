package com.samnammae.menu_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionRequestDto {
    private String name;
    private int price;
    private boolean isDefault;
}