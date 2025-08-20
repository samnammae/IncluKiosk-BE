package com.samnammae.order_service.domain.selectedoption;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SelectedOption {

    private String optionCategoryName;
    private String optionName;
    private int optionPrice;
}
