package com.samnammae.admin_service.controller;

import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.service.StoreService;
import com.samnammae.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;

@RestController
@RequestMapping("/api/admin/store")
@Tag(name = "Admin", description = "관리자 관련 API")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<Long> registerStore(@ModelAttribute StoreRequest request) {
        Long storeId = storeService.createStore(request);
        return ApiResponse.success(storeId);
    }
}
