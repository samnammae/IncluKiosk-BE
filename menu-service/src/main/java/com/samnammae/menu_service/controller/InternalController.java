package com.samnammae.menu_service.controller;

import com.samnammae.common.response.ApiResponse;
import com.samnammae.menu_service.dto.response.MenuWithOptionsResponseDto;
import com.samnammae.menu_service.service.InternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/menu")
@Tag(name = "Internal Menu", description = "내부 서비스용 메뉴 API")
@RequiredArgsConstructor
public class InternalController {

    private final InternalService internalService;

    @GetMapping("/{storeId}/kiosk")
    @Operation(summary = "키오스크용 메뉴 조회", description = "LLM 처리를 위한 옵션 상세 정보가 포함된 전체 메뉴를 조회합니다.")
    public ApiResponse<MenuWithOptionsResponseDto> getMenusWithOptions(
            @PathVariable Long storeId) {

        MenuWithOptionsResponseDto response = internalService.getMenusWithOptions(storeId);

        return ApiResponse.success(response);
    }
}