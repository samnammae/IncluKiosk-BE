package com.samnammae.order_service.domain.orderitem;

import com.samnammae.order_service.domain.selectedoption.SelectedOption;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long menuId;
    private String menuName;
    private int basePrice;
    private int quantity;
    private List<SelectedOption> selectedOptions; // 선택 옵션 목록을 내장
    private int itemTotalPrice;
}
