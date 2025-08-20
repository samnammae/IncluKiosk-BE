package com.samnammae.menu_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.dto.request.MenuCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuDetailResponseDto;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "[1,2]", false);
        Long menuId = 1L;

        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes());
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "request.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8));

        doNothing().when(menuService).validateStoreAccess(storeId, managedStoreIds);
        when(menuService.createMenu(eq(storeId), any(MenuCreateRequestDto.class), any()))
                .thenReturn(menuId);

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(requestFile)
                        .file(image)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
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
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "[1,2]", false);
        Long menuId = 1L;

        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "request.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8));

        doNothing().when(menuService).validateStoreAccess(storeId, managedStoreIds);
        when(menuService.createMenu(eq(storeId), any(MenuCreateRequestDto.class), isNull()))
                .thenReturn(menuId);

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(requestFile)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
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
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "[1,2]", false);

        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "request.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8));

        doThrow(new CustomException(ErrorCode.STORE_ACCESS_DENIED))
                .when(menuService).validateStoreAccess(storeId, managedStoreIds);

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(requestFile)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
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

        doNothing().when(menuService).validateStoreAccess(storeId, managedStoreIds);
        when(menuService.getMenusByStore(storeId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/menu/{storeId}", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.categories.length()").value(2));

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
                "수정된 치킨버거", 9000, "더 맛있는 치킨버거", 1L, "[1,2]", false);

        MockMultipartFile image = new MockMultipartFile(
                "image", "updated.jpg", MediaType.IMAGE_JPEG_VALUE, "updated content".getBytes());
        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "request.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8));

        doNothing().when(menuService).validateStoreAccess(storeId, managedStoreIds);
        when(menuService.updateMenu(eq(storeId), eq(menuId), any(MenuUpdateRequestDto.class), any()))
                .thenReturn(menuId);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/menu/{storeId}/{menuId}", storeId, menuId)
                        .file(requestFile)
                        .file(image)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PUT");
                            return httpRequest;
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
                "수정된 치킨버거", 9000, "더 맛있는 치킨버거", 1L, "[1,2]", false);

        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "request.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8));

        doNothing().when(menuService).validateStoreAccess(storeId, managedStoreIds);
        when(menuService.updateMenu(eq(storeId), eq(menuId), any(MenuUpdateRequestDto.class), any()))
                .thenThrow(new CustomException(ErrorCode.MENU_NOT_FOUND));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/menu/{storeId}/{menuId}", storeId, menuId)
                        .file(requestFile)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PUT");
                            return httpRequest;
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

        doNothing().when(menuService).validateStoreAccess(storeId, managedStoreIds);
        doNothing().when(menuService).deleteMenu(storeId, menuId);

        // When & Then
        mockMvc.perform(delete("/api/menu/{storeId}/{menuId}", storeId, menuId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

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

        doNothing().when(menuService).validateStoreAccess(storeId, managedStoreIds);
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
    @DisplayName("메뉴 상세 조회 - 성공")
    void getMenuDetail_Success() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;

        MenuDetailResponseDto.OptionDto optionDto = MenuDetailResponseDto.OptionDto.builder()
                .optionId(1L)
                .optionName("라지")
                .price(1000)
                .build();

        MenuDetailResponseDto.OptionCategoryDto categoryDto = MenuDetailResponseDto.OptionCategoryDto.builder()
                .categoryId(1L)
                .categoryName("사이즈")
                .options(Arrays.asList(optionDto))
                .build();

        MenuDetailResponseDto response = MenuDetailResponseDto.builder()
                .menuId(1L)
                .menuName("치킨버거")
                .basePrice(8000)
                .description("맛있는 치킨버거")
                .imageUrl("https://example.com/image.jpg")
                .isSoldOut(false)
                .optionCategories(Arrays.asList(categoryDto))
                .build();

        when(menuService.getMenuDetail(storeId, menuId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/menu/{storeId}/{menuId}", storeId, menuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.menuId").value(1))
                .andExpect(jsonPath("$.data.menuName").value("치킨버거"))
                .andExpect(jsonPath("$.data.basePrice").value(8000))
                .andExpect(jsonPath("$.data.soldOut").value(false));

        verify(menuService).getMenuDetail(storeId, menuId);
    }

    @Test
    @DisplayName("메뉴 상세 조회 - 메뉴 없음")
    void getMenuDetail_MenuNotFound() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 999L;

        when(menuService.getMenuDetail(storeId, menuId))
                .thenThrow(new CustomException(ErrorCode.MENU_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/menu/{storeId}/{menuId}", storeId, menuId))
                .andExpect(status().isNotFound());

        verify(menuService).getMenuDetail(storeId, menuId);
    }

    @Test
    @DisplayName("메뉴 상세 조회 - 매장 불일치")
    void getMenuDetail_StoreMismatch() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;

        when(menuService.getMenuDetail(storeId, menuId))
                .thenThrow(new CustomException(ErrorCode.MENU_STORE_MISMATCH));

        // When & Then
        mockMvc.perform(get("/api/menu/{storeId}/{menuId}", storeId, menuId))
                .andExpect(status().isForbidden());

        verify(menuService).getMenuDetail(storeId, menuId);
    }

    @Test
    @DisplayName("헤더 누락 - 매장 ID 헤더 없음")
    void missingHeader() throws Exception {
        // Given
        Long storeId = 1L;
        MenuCreateRequestDto requestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "[1,2]", false);

        MockMultipartFile requestFile = new MockMultipartFile(
                "request", "request.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipart("/api/menu/{storeId}", storeId)
                        .file(requestFile))
                .andExpect(status().isBadRequest());

        verify(menuService, never()).validateStoreAccess(anyLong(), anyString());
    }
}