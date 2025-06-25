package com.samnammae.admin_service.controller;

import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.service.StoreService;
import com.samnammae.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/store")
@Tag(name = "Admin", description = "관리자 관련 API")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "매장 등록", description = "매장 정보를 입력하여 새로운 매장을 등록합니다.")
    public ApiResponse<Long> registerStore(
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("request") StoreRequest request,
            @RequestPart(value = "mainImg", required = false) MultipartFile mainImg,
            @RequestPart(value = "logoImg", required = false) MultipartFile logoImg,
            @RequestPart(value = "startBackground", required = false) MultipartFile startBackground
    ) {
        request.setMainImg(mainImg);
        request.setLogoImg(logoImg);
        request.setStartBackground(startBackground);
        Long storeId = storeService.createStore(userId, request);
        return ApiResponse.success(storeId);
    }
}