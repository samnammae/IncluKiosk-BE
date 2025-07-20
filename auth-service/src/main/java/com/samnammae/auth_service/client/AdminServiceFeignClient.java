package com.samnammae.auth_service.client;

import com.samnammae.auth_service.dto.response.StoreSimpleResponse;
import com.samnammae.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "admin-service")
public interface AdminServiceFeignClient {

    @GetMapping("/api/admin/store")
    ApiResponse<List<StoreSimpleResponse>> getStoresByUserId(@RequestHeader("X-User-Id") Long userId);
}
