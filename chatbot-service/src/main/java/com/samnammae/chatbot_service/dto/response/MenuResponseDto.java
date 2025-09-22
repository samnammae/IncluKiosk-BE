package com.samnammae.chatbot_service.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class MenuResponseDto {
    private List<String> categories;
    private Map<String, List<MenuItemDto>> menusByCategory;

    @Getter
    @NoArgsConstructor
    public static class MenuItemDto {
        private String id;
        private String name;
        private int price;
        private String description;
        private String image;
        private List<String> optionCategories;
        private boolean isSoldOut;
    }
}