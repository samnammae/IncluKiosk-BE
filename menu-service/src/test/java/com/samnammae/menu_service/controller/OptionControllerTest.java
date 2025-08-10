package com.samnammae.menu_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.dto.request.OptionCategoryRequestDto;
import com.samnammae.menu_service.dto.response.OptionCategoryResponseDto;
import com.samnammae.menu_service.service.OptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionController.class)
class OptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OptionService optionService;

    private final Long storeId = 1L;
    private final Long categoryId = 1L;
    private final Long optionId = 101L;
    private final String managedStoreIds = "1,2,3";

    @Test
    @DisplayName("옵션 생성 - 성공")
    void createOptionCategory_Success() throws Exception {
        // Given
        OptionCategoryRequestDto requestDto = new OptionCategoryRequestDto(); // DTO 필드 설정 필요
        when(optionService.createOptionCategory(eq(storeId), any(OptionCategoryRequestDto.class)))
                .thenReturn(categoryId);

        // When & Then
        mockMvc.perform(post("/api/menu/{storeId}/option", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(categoryId));

        verify(optionService).validateStoreAccess(storeId, managedStoreIds);
        verify(optionService).createOptionCategory(eq(storeId), any(OptionCategoryRequestDto.class));
    }

    @Test
    @DisplayName("옵션 생성 - 매장 접근 권한 없음")
    void createOptionCategory_AccessDenied() throws Exception {
        // Given
        OptionCategoryRequestDto requestDto = new OptionCategoryRequestDto();
        doThrow(new CustomException(ErrorCode.STORE_ACCESS_DENIED))
                .when(optionService).validateStoreAccess(storeId, managedStoreIds);

        // When & Then
        mockMvc.perform(post("/api/menu/{storeId}/option", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(optionService).validateStoreAccess(storeId, managedStoreIds);
        verify(optionService, never()).createOptionCategory(any(), any());
    }

    @Test
    @DisplayName("옵션 목록 조회 - 성공")
    void getOptions_Success() throws Exception {
        // Given
        List<OptionCategoryResponseDto> response = Collections.emptyList(); // 테스트용 응답 DTO 생성 필요
        when(optionService.getOptionsByStore(storeId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/menu/{storeId}/option", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(optionService).validateStoreAccess(storeId, managedStoreIds);
        verify(optionService).getOptionsByStore(storeId);
    }

    @Test
    @DisplayName("옵션 수정 - 성공")
    void updateOptionCategory_Success() throws Exception {
        // Given
        OptionCategoryRequestDto requestDto = new OptionCategoryRequestDto();
        when(optionService.updateOptionCategory(eq(storeId), eq(categoryId), any(OptionCategoryRequestDto.class)))
                .thenReturn(categoryId);

        // When & Then
        mockMvc.perform(put("/api/menu/{storeId}/option/{optionCategoryId}", storeId, categoryId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(categoryId));

        verify(optionService).validateStoreAccess(storeId, managedStoreIds);
        verify(optionService).updateOptionCategory(eq(storeId), eq(categoryId), any(OptionCategoryRequestDto.class));
    }

    @Test
    @DisplayName("옵션 수정 - 카테고리 없음")
    void updateOptionCategory_NotFound() throws Exception {
        // Given
        OptionCategoryRequestDto requestDto = new OptionCategoryRequestDto();
        when(optionService.updateOptionCategory(eq(storeId), eq(categoryId), any(OptionCategoryRequestDto.class)))
                .thenThrow(new CustomException(ErrorCode.OPTION_CATEGORY_NOT_FOUND));

        // When & Then
        mockMvc.perform(put("/api/menu/{storeId}/option/{optionCategoryId}", storeId, categoryId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("옵션 카테고리 삭제 - 성공")
    void deleteOptionCategory_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/option/{optionCategoryId}", storeId, categoryId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(optionService).validateStoreAccess(storeId, managedStoreIds);
        verify(optionService).deleteOptionCategory(storeId, categoryId);
    }

    @Test
    @DisplayName("개별 옵션 삭제 - 성공")
    void deleteOption_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/option/{optionCategoryId}/{optionId}", storeId, categoryId, optionId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(optionService).validateStoreAccess(storeId, managedStoreIds);
        verify(optionService).deleteOption(storeId, categoryId, optionId);
    }

    @Test
    @DisplayName("개별 옵션 삭제 - 옵션 없음")
    void deleteOption_NotFound() throws Exception {
        // Given
        doThrow(new CustomException(ErrorCode.OPTION_NOT_FOUND))
                .when(optionService).deleteOption(storeId, categoryId, optionId);

        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/option/{optionCategoryId}/{optionId}", storeId, categoryId, optionId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isNotFound());
    }
}