package com.samnammae.menu_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryResponseDto {
    private Long id;
    private String name;
    private int displayOrder;
}
