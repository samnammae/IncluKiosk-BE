package com.samnammae.order_service.client;

import com.samnammae.common.response.ApiResponse;
import com.samnammae.order_service.dto.response.MenuDetailResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "menu-service")
public interface MenuServiceFeignClient {

    @GetMapping("/api/menu/{storeId}/{menuId}")
    ApiResponse<MenuDetailResponseDto> getMenuDetails(@PathVariable Long storeId, @PathVariable Long menuId);
}
