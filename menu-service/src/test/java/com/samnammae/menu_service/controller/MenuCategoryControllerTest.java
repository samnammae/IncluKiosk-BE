package com.samnammae.menu_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.dto.request.MenuCategoryCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuCategoryUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuCategoryResponseDto;
import com.samnammae.menu_service.service.MenuCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuCategoryController.class)
class MenuCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuCategoryService menuCategoryService;

    @Test
    @DisplayName("카테고리 생성 - 성공")
    void createCategory_Success() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";
        MenuCategoryCreateRequestDto requestDto = new MenuCategoryCreateRequestDto("메인 메뉴", 1);
        Long categoryId = 1L;

        when(menuCategoryService.createCategory(eq(storeId), any(MenuCategoryCreateRequestDto.class)))
                .thenReturn(categoryId);

        // When & Then
        mockMvc.perform(post("/api/menu/{storeId}/category", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).createCategory(eq(storeId), any(MenuCategoryCreateRequestDto.class));

    }

    @Test
    @DisplayName("카테고리 생성 - 매장 접근 권한 없음")
    void createCategory_AccessDenied() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "2,3,4";
        MenuCategoryCreateRequestDto requestDto = new MenuCategoryCreateRequestDto("메인 메뉴", 1);

        doThrow(new CustomException(ErrorCode.STORE_ACCESS_DENIED))
                .when(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);

        // When & Then
        mockMvc.perform(post("/api/menu/{storeId}/category", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService, never()).createCategory(any(), any());
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 성공")
    void getCategories_Success() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";
        List<MenuCategoryResponseDto> response = Arrays.asList(
                new MenuCategoryResponseDto(1L, "메인 메뉴", 1),
                new MenuCategoryResponseDto(2L, "사이드 메뉴", 2)
        );

        when(menuCategoryService.getCategories(storeId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/menu/{storeId}/category", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("메인 메뉴"))
                .andExpect(jsonPath("$.data[1].name").value("사이드 메뉴"));

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).getCategories(storeId);
    }

    @Test
    @DisplayName("카테고리 일괄 수정 - 성공")
    void updateCategories_Success() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";
        List<MenuCategoryUpdateRequestDto> requestDto = Arrays.asList(
                new MenuCategoryUpdateRequestDto(1L, "수정된 메뉴", 1)
        );
        List<Long> updatedIds = Arrays.asList(1L);

        when(menuCategoryService.updateCategories(eq(storeId), anyList())).thenReturn(updatedIds);

        // When & Then
        mockMvc.perform(patch("/api/menu/{storeId}/category", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).updateCategories(eq(storeId), anyList());
    }

    @Test
    @DisplayName("카테고리 일괄 수정 - 카테고리 없음")
    void updateCategories_CategoryNotFound() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";
        List<MenuCategoryUpdateRequestDto> requestDto = Arrays.asList(
                new MenuCategoryUpdateRequestDto(999L, "수정된 메뉴", 1)
        );

        when(menuCategoryService.updateCategories(eq(storeId), anyList()))
                .thenThrow(new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // When & Then
        mockMvc.perform(patch("/api/menu/{storeId}/category", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).updateCategories(eq(storeId), anyList());
    }

    @Test
    @DisplayName("카테고리 일괄 수정 - 카테고리 접근 권한 없음")
    void updateCategories_CategoryAccessDenied() throws Exception {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";
        List<MenuCategoryUpdateRequestDto> requestDto = Arrays.asList(
                new MenuCategoryUpdateRequestDto(1L, "수정된 메뉴", 1)
        );

        when(menuCategoryService.updateCategories(eq(storeId), anyList()))
                .thenThrow(new CustomException(ErrorCode.CATEGORY_ACCESS_DENIED));

        // When & Then
        mockMvc.perform(patch("/api/menu/{storeId}/category", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).updateCategories(eq(storeId), anyList());
    }

    @Test
    @DisplayName("카테고리 삭제 - 성공")
    void deleteCategory_Success() throws Exception {
        // Given
        Long storeId = 1L;
        Long categoryId = 1L;
        String managedStoreIds = "1,2,3";

        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/category/{categoryId}", storeId, categoryId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).deleteCategory(storeId, categoryId);
    }

    @Test
    @DisplayName("카테고리 삭제 - 카테고리 없음")
    void deleteCategory_CategoryNotFound() throws Exception {
        // Given
        Long storeId = 1L;
        Long categoryId = 999L;
        String managedStoreIds = "1,2,3";

        doThrow(new CustomException(ErrorCode.CATEGORY_NOT_FOUND))
                .when(menuCategoryService).deleteCategory(storeId, categoryId);

        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/category/{categoryId}", storeId, categoryId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isNotFound());

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).deleteCategory(storeId, categoryId);
    }

    @Test
    @DisplayName("헤더 누락 - 매장 ID 헤더 없음")
    void missingHeader() throws Exception {
        // Given
        Long storeId = 1L;
        MenuCategoryCreateRequestDto requestDto = new MenuCategoryCreateRequestDto("메인 메뉴", 1);

        // When & Then
        mockMvc.perform(post("/api/menu/{storeId}/category", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(menuCategoryService, never()).validateStoreAccess(anyLong(), anyString());
    }

    @Test
    @DisplayName("카테고리 삭제 - 카테고리 접근 권한 없음")
    void deleteCategory_CategoryAccessDenied() throws Exception {
        // Given
        Long storeId = 1L;
        Long categoryId = 1L;
        String managedStoreIds = "1,2,3";

        doThrow(new CustomException(ErrorCode.CATEGORY_ACCESS_DENIED))
                .when(menuCategoryService).deleteCategory(storeId, categoryId);

        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/category/{categoryId}", storeId, categoryId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isForbidden());

        verify(menuCategoryService).validateStoreAccess(storeId, managedStoreIds);
        verify(menuCategoryService).deleteCategory(storeId, categoryId);
    }
}