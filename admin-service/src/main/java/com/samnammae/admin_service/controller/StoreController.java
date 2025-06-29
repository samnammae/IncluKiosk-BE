package com.samnammae.admin_service.controller;

import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.dto.response.StoreResponse;
import com.samnammae.admin_service.dto.response.StoreSimpleResponse;
import com.samnammae.admin_service.service.StoreService;
import com.samnammae.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    // 매장 목록 조회
    @GetMapping
    @Operation(summary = "매장 목록 조회", description = "관리자가 등록한 매장들의 간단한 정보를 조회합니다.")
    public ApiResponse<List<StoreSimpleResponse>> getStoreList(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<StoreSimpleResponse> storeList = storeService.getStoreList(userId);
        return ApiResponse.success(storeList);
    }

    // 특정 매장 조회
    @GetMapping("/{storeId}")
    @Operation(summary = "특정 매장 조회", description = "매장 ID를 통해 특정 매장의 상세 정보를 조회합니다.")
    public ApiResponse<StoreResponse> getStore(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long storeId
    ) {
        StoreResponse store = storeService.getStore(userId, storeId);
        return ApiResponse.success(store);
    }

    // 매장 정보 수정
    @PutMapping("/{storeId}")
    @Operation(summary = "매장 정보 수정", description = "매장 ID를 통해 매장 정보를 수정합니다.")
    public ApiResponse<StoreResponse> updateStore(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long storeId,
            @RequestPart("request") StoreRequest request,
            @RequestPart(value = "mainImg", required = false) MultipartFile mainImg,
            @RequestPart(value = "logoImg", required = false) MultipartFile logoImg,
            @RequestPart(value = "startBackground", required = false) MultipartFile startBackground
    ) {
        request.setMainImg(mainImg);
        request.setLogoImg(logoImg);
        request.setStartBackground(startBackground);
        StoreResponse store = storeService.updateStore(userId, storeId, request);
        return ApiResponse.success(store);
    }
}