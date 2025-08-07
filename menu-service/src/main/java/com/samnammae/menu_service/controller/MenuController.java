package com.samnammae.menu_service.controller;

import com.samnammae.common.response.ApiResponse;
import com.samnammae.menu_service.dto.request.MenuCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuListResponseDto;
import com.samnammae.menu_service.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Menu", description = "메뉴 관리 API")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping(value = "/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "메뉴 추가", description = "새로운 메뉴를 추가합니다.")
    public ApiResponse<Long> createMenu(
            @PathVariable Long storeId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds,
            @RequestPart("request") MenuCreateRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // 매장 접근 권한 검증
        menuService.validateStoreAccess(storeId, managedStoreIds);

        // 메뉴 생성
        Long menuId = menuService.createMenu(storeId, requestDto, image);

        return ApiResponse.success(menuId);
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "메뉴 목록 조회", description = "특정 매장의 전체 메뉴를 카테고리별로 그룹핑하여 조회합니다.")
    public ApiResponse<MenuListResponseDto> getMenus(
            @PathVariable Long storeId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {

        // 매장 접근 권한 검증
        menuService.validateStoreAccess(storeId, managedStoreIds);

        // 메뉴 목록 조회
        MenuListResponseDto response = menuService.getMenusByStore(storeId);

        return ApiResponse.success(response);
    }

    @PutMapping(value = "/{storeId}/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "메뉴 수정", description = "기존 메뉴의 정보를 수정합니다.")
    public ApiResponse<Long> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds,
            @RequestPart("request") MenuUpdateRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // 매장 접근 권한 검증
        menuService.validateStoreAccess(storeId, managedStoreIds);

        // 메뉴 수정
        Long updatedMenuId = menuService.updateMenu(storeId, menuId, requestDto, image);

        return ApiResponse.success(updatedMenuId);
    }

    @DeleteMapping("/{storeId}/{menuId}")
    @Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다.")
    public ApiResponse<Void> deleteMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {

        // 매장 접근 권한 검증
        menuService.validateStoreAccess(storeId, managedStoreIds);

        // 메뉴 삭제
        menuService.deleteMenu(storeId, menuId);

        return ApiResponse.success(null);
    }
}
