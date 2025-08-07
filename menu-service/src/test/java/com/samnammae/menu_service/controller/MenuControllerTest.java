package com.samnammae.menu_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.dto.request.MenuCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuListResponseDto;
import com.samnammae.menu_service.dto.response.MenuResponseDto;
import com.samnammae.menu_service.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuService menuService;

    @Test
    @DisplayName("메뉴 생성 - 성공 (이미지 있음)")
    void createMenu_WithImage_Success() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";
        MenuCreateRequestDto requestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "1,2", false);
        Long menuId = 1L;

        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test content".getBytes());
        MockMultipartFile request = new MockMultipartFile(
                "request", "", "application/json", objectMapper.writeValueAsString(requestDto).getBytes());

        when(menuService.createMenu(eq(storeId), any(MenuCreateRequestDto.class), any()))
                .thenReturn(menuId);

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(request)
                        .file(image)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService).createMenu(eq(storeId), any(MenuCreateRequestDto.class), any());
    }

    @Test
    @DisplayName("메뉴 생성 - 성공 (이미지 없음)")
    void createMenu_WithoutImage_Success() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";
        MenuCreateRequestDto requestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "1,2", false);
        Long menuId = 1L;

        MockMultipartFile request = new MockMultipartFile(
                "request", "", "application/json", objectMapper.writeValueAsString(requestDto).getBytes());

        when(menuService.createMenu(eq(storeId), any(MenuCreateRequestDto.class), isNull()))
                .thenReturn(menuId);

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(request)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService).createMenu(eq(storeId), any(MenuCreateRequestDto.class), isNull());
    }

    @Test
    @DisplayName("메뉴 생성 - 매장 접근 권한 없음")
    void createMenu_AccessDenied() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "2,3,4";
        MenuCreateRequestDto requestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "1,2", false);

        MockMultipartFile request = new MockMultipartFile(
                "request", "", "application/json", objectMapper.writeValueAsString(requestDto).getBytes());

        doThrow(new CustomException(ErrorCode.STORE_ACCESS_DENIED))
                .when(menuService).validateStoreAccess(storeId, managedStoreIds);

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(request)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService, never()).createMenu(any(), any(), any());
    }

    @Test
    @DisplayName("메뉴 목록 조회 - 성공")
    void getMenus_Success() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";

        List<String> categories = Arrays.asList("메인 메뉴", "사이드 메뉴");
        Map<String, List<MenuResponseDto>> menusByCategory = new HashMap<>();
        menusByCategory.put("메인 메뉴", Arrays.asList(
                new MenuResponseDto(1L, "치킨버거", 8000, "맛있는 치킨버거",
                        "https://example.com/image.jpg", Arrays.asList(1L, 2L), false)
        ));

        MenuListResponseDto response = new MenuListResponseDto(categories, menusByCategory);

        when(menuService.getMenusByStore(storeId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/menu/{storeId}", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.categories.length()").value(2))
                .andExpect(jsonPath("$.data.categories[0]").value("메인 메뉴"))
                .andExpect(jsonPath("$.data.menusByCategory['메인 메뉴'][0].name").value("치킨버거"));

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService).getMenusByStore(storeId);
    }

    @Test
    @DisplayName("메뉴 수정 - 성공")
    void updateMenu_Success() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;
        String managedStoreIds = "1,2,3";
        MenuUpdateRequestDto requestDto = new MenuUpdateRequestDto(
                "수정된 치킨버거", 9000, "더 맛있는 치킨버거", 1L, "1,2", false);

        MockMultipartFile image = new MockMultipartFile(
                "image", "updated.jpg", "image/jpeg", "updated content".getBytes());
        MockMultipartFile request = new MockMultipartFile(
                "request", "", "application/json", objectMapper.writeValueAsString(requestDto).getBytes());

        when(menuService.updateMenu(eq(storeId), eq(menuId), any(MenuUpdateRequestDto.class), any()))
                .thenReturn(menuId);

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}/{menuId}", storeId, menuId)
                        .file(request)
                        .file(image)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setMethod("PUT");
                            return requestPostProcessor;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService).updateMenu(eq(storeId), eq(menuId), any(MenuUpdateRequestDto.class), any());
    }

    @Test
    @DisplayName("메뉴 수정 - 메뉴 없음")
    void updateMenu_MenuNotFound() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 999L;
        String managedStoreIds = "1,2,3";
        MenuUpdateRequestDto requestDto = new MenuUpdateRequestDto(
                "수정된 치킨버거", 9000, "더 맛있는 치킨버거", 1L, "1,2", false);

        MockMultipartFile request = new MockMultipartFile(
                "request", "", "application/json", objectMapper.writeValueAsString(requestDto).getBytes());

        when(menuService.updateMenu(eq(storeId), eq(menuId), any(MenuUpdateRequestDto.class), any()))
                .thenThrow(new CustomException(ErrorCode.MENU_NOT_FOUND));

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}/{menuId}", storeId, menuId)
                        .file(request)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setMethod("PUT");
                            return requestPostProcessor;
                        }))
                .andExpect(status().isNotFound());

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService).updateMenu(eq(storeId), eq(menuId), any(MenuUpdateRequestDto.class), any());
    }

    @Test
    @DisplayName("메뉴 삭제 - 성공")
    void deleteMenu_Success() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;
        String managedStoreIds = "1,2,3";

        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/{menuId}", storeId, menuId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService).deleteMenu(storeId, menuId);
    }

    @Test
    @DisplayName("메뉴 삭제 - 메뉴 없음")
    void deleteMenu_MenuNotFound() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 999L;
        String managedStoreIds = "1,2,3";

        doThrow(new CustomException(ErrorCode.MENU_NOT_FOUND))
                .when(menuService).deleteMenu(storeId, menuId);

        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/{menuId}", storeId, menuId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isNotFound());

        verify(menuService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuService).deleteMenu(storeId, menuId);
    }

    @Test
    @DisplayName("헤더 누락 - 매장 ID 헤더 없음")
    void missingHeader() throws Exception {
        // Given
        Long storeId = 1L;
        MenuCreateRequestDto requestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "1,2", false);

        MockMultipartFile request = new MockMultipartFile(
                "request", "", "application/json", objectMapper.writeValueAsString(requestDto).getBytes());

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(request)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(menuService, never()).validateStoreAccess(anyLong(), anyString());
    }
}