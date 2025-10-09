package com.samnammae.chatbot_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuWithOptionsResponseDto {
    private List<String> categories;
    private Map<String, List<MenuDetail>> menusByCategory;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuDetail {
        private Long id;
        private String name;
        private int price;
        private String description;
        private List<OptionCategory> optionCategories;

        @JsonProperty("isSoldOut")
        private boolean soldOut;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionCategory {
        private Long id;
        private String name;
        private String type;
        private boolean required;
        private List<Option> options;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private Long id;
        private String name;
        private int price;

        @JsonProperty("default")
        private boolean isDefault;
    }
}