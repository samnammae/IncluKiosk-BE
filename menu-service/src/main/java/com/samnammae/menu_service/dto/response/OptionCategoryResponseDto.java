package com.samnammae.menu_service.dto.response;

import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OptionCategoryResponseDto {
    private final Long id;
    private final String name;
    private final String type;
    private final boolean required;
    private final List<OptionResponseDto> options;

    public OptionCategoryResponseDto(OptionCategory category) {
        this.id = category.getId();
        this.name = category.getName();
        this.type = category.getType().name();
        this.required = category.isRequired();
        this.options = category.getOptions().stream()
                .map(OptionResponseDto::new)
                .collect(Collectors.toList());
    }
}