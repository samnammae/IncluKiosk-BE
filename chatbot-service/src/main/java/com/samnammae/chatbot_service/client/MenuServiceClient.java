package com.samnammae.chatbot_service.client;

import com.samnammae.chatbot_service.dto.response.MenuResponseDto;
import com.samnammae.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "menu-service")
public interface MenuServiceClient {

    @GetMapping("/api/menu/{storeId}")
    ApiResponse<MenuResponseDto> getMenusByStore(@PathVariable Long storeId,
                                                 @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds); // 반환 타입 수정
}
