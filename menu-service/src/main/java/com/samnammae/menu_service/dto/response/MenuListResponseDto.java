package com.samnammae.menu_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuListResponseDto {
    private List<String> categories;
    private Map<String, List<MenuResponseDto>> menusByCategory;
}
