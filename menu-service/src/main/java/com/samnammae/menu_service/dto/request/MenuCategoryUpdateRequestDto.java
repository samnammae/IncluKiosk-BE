package com.samnammae.menu_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryUpdateRequestDto {
    private Long id;
    private String name;
    private int displayOrder;
}