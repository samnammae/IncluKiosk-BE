package com.samnammae.menu_service.controller;

import com.samnammae.common.response.ApiResponse;
import com.samnammae.menu_service.dto.request.OptionCategoryRequestDto;
import com.samnammae.menu_service.dto.response.OptionCategoryResponseDto;
import com.samnammae.menu_service.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Option", description = "옵션 관리 API")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @PostMapping("/{storeId}/option")
    @Operation(summary = "옵션 추가", description = "새로운 옵션 카테고리와 하위 옵션들을 추가합니다.")
    public ApiResponse<Long> createOptionCategory(
            @PathVariable Long storeId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds,
            @RequestBody OptionCategoryRequestDto requestDto) {

        // 매장 접근 권한 검증
        optionService.validateStoreAccess(storeId, managedStoreIds);

        // 옵션 카테고리 생성
        Long optionCategoryId = optionService.createOptionCategory(storeId, requestDto);

        return ApiResponse.success(optionCategoryId);
    }

    @GetMapping("/{storeId}/option")
    @Operation(summary = "옵션 목록 조회", description = "특정 매장의 전체 옵션을 조회합니다.")
    public ApiResponse<List<OptionCategoryResponseDto>> getOptions(
            @PathVariable Long storeId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {

        // 매장 접근 권한 검증
        optionService.validateStoreAccess(storeId, managedStoreIds);

        // 옵션 목록 조회
        List<OptionCategoryResponseDto> response = optionService.getOptionsByStore(storeId);

        return ApiResponse.success(response);
    }

    @PutMapping("/{storeId}/option/{optionCategoryId}")
    @Operation(summary = "옵션 수정", description = "기존 옵션 카테고리와 하위 옵션들을 수정합니다.")
    public ApiResponse<Long> updateOptionCategory(
            @PathVariable Long storeId,
            @PathVariable Long optionCategoryId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds,
            @RequestBody OptionCategoryRequestDto requestDto) {

        // 매장 접근 권한 검증
        optionService.validateStoreAccess(storeId, managedStoreIds);

        // 옵션 카테고리 수정
        Long updatedId = optionService.updateOptionCategory(storeId, optionCategoryId, requestDto);

        return ApiResponse.success(updatedId);
    }

    @DeleteMapping("/{storeId}/option/{optionCategoryId}")
    @Operation(summary = "옵션 카테고리 삭제", description = "옵션 카테고리 및 하위 옵션 전체를 삭제합니다.")
    public ApiResponse<Void> deleteOptionCategory(
            @PathVariable Long storeId,
            @PathVariable Long optionCategoryId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {

        // 매장 접근 권한 검증
        optionService.validateStoreAccess(storeId, managedStoreIds);

        // 옵션 카테고리 삭제
        optionService.deleteOptionCategory(storeId, optionCategoryId);

        return ApiResponse.success(null);
    }

    @DeleteMapping("/{storeId}/option/{optionCategoryId}/{optionId}")
    @Operation(summary = "개별 옵션 삭제", description = "특정 카테고리 내의 개별 옵션을 삭제합니다.")
    public ApiResponse<Void> deleteOption(
            @PathVariable Long storeId,
            @PathVariable Long optionCategoryId,
            @PathVariable Long optionId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {

        // 매장 접근 권한 검증
        optionService.validateStoreAccess(storeId, managedStoreIds);

        // 개별 옵션 삭제
        optionService.deleteOption(storeId, optionCategoryId, optionId);

        return ApiResponse.success(null);
    }
}