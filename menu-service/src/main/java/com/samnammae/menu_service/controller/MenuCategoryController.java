package com.samnammae.menu_service.controller;

import com.samnammae.common.response.ApiResponse;
import com.samnammae.menu_service.dto.request.MenuCategoryCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuCategoryUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuCategoryResponseDto;
import com.samnammae.menu_service.service.MenuCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Menu Category", description = "메뉴 카테고리 관리 API")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    @PostMapping("/{storeId}/category")
    @Operation(summary = "메뉴 카테고리 추가", description = "특정 매장에 새로운 메뉴 카테고리를 추가합니다.")
    public ApiResponse<Long> createCategory(
            @PathVariable Long storeId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds,
            @RequestBody MenuCategoryCreateRequestDto requestDto) {
        // 매장 접근 권한 검증
        menuCategoryService.validateStoreAccess(storeId, managedStoreIds);

        // 카테고리 생성
        Long categoryId = menuCategoryService.createCategory(storeId, requestDto);

        return ApiResponse.success(categoryId);
    }

    @GetMapping("/{storeId}/category")
    @Operation(summary = "메뉴 카테고리 목록 조회", description = "특정 매장의 모든 메뉴 카테고리를 조회합니다.")
    public ApiResponse<List<MenuCategoryResponseDto>> getCategories(
            @PathVariable Long storeId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {
        // 매장 접근 권한 검증
        menuCategoryService.validateStoreAccess(storeId, managedStoreIds);

        // 카테고리 목록 조회
        List<MenuCategoryResponseDto> response = menuCategoryService.getCategories(storeId);

        return ApiResponse.success(response);
    }

    @PatchMapping("/{storeId}/category")
    @Operation(summary = "메뉴 카테고리 수정", description = "특정 매장의 메뉴 카테고리 목록을 일괄 수정합니다. (순서 변경 등)")
    public ApiResponse<List<Long>> updateCategories(
            @PathVariable Long storeId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds,
            @RequestBody List<MenuCategoryUpdateRequestDto> requestDto) {
        // 매장 접근 권한 검증
        menuCategoryService.validateStoreAccess(storeId, managedStoreIds);

        // 카테고리 목록 수정
        List<Long> updatedCategoryIds = menuCategoryService.updateCategories(storeId, requestDto);

        return ApiResponse.success(updatedCategoryIds);
    }

    @DeleteMapping("/{storeId}/category/{categoryId}")
    @Operation(summary = "메뉴 카테고리 삭제", description = "특정 매장의 메뉴 카테고리를 삭제합니다.")
    public ApiResponse<Void> deleteCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {
        // 매장 접근 권한 검증
        menuCategoryService.validateStoreAccess(storeId, managedStoreIds);

        // 카테고리 삭제
        menuCategoryService.deleteCategory(storeId, categoryId);

        return ApiResponse.success(null);
    }
}
