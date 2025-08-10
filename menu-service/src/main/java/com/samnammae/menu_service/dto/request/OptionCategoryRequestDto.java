package com.samnammae.menu_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionCategoryRequestDto {
    private String name;
    private String type; // "SINGLE" 또는 "MULTIPLE"
    private boolean required;
    private List<OptionRequestDto> options;
}