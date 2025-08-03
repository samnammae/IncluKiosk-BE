package com.samnammae.menu_service.service;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.domain.menu.MenuRepository;
import com.samnammae.menu_service.domain.menucategory.MenuCategory;
import com.samnammae.menu_service.domain.menucategory.MenuCategoryRepository;
import com.samnammae.menu_service.dto.request.MenuCategoryCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuCategoryUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuCategoryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuCategoryServiceTest {

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuCategoryService menuCategoryService;

    private MenuCategory testCategory;
    private MenuCategoryCreateRequestDto createRequestDto;
    private MenuCategoryUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        testCategory = MenuCategory.builder()
                .storeId(1L)
                .name("메인 메뉴")
                .displayOrder(1)
                .build();

        createRequestDto = new MenuCategoryCreateRequestDto("메인 메뉴", 1);
        updateRequestDto = new MenuCategoryUpdateRequestDto(1L, "수정된 메뉴", 2);
    }

    @Test
    @DisplayName("매장 접근 권한 검증 - 성공")
    void validateStoreAccess_Success() {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";

        // When & Then
        assertDoesNotThrow(() -> menuCategoryService.validateStoreAccess(storeId, managedStoreIds));
    }

    @Test
    @DisplayName("매장 접근 권한 검증 - 실패")
    void validateStoreAccess_Fail() {
        // Given
        Long storeId = 4L;
        String managedStoreIds = "1,2,3";

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuCategoryService.validateStoreAccess(storeId, managedStoreIds));
        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 생성 - 성공")
    void createCategory_Success() {
        // Given
        Long storeId = 1L;
        MenuCategory savedCategory = MenuCategory.builder()
                .id(1L)
                .storeId(storeId)
                .name("메인 메뉴")
                .displayOrder(1)
                .build();

        when(menuCategoryRepository.save(any(MenuCategory.class))).thenReturn(savedCategory);

        // When
        Long result = menuCategoryService.createCategory(storeId, createRequestDto);

        // Then
        assertEquals(1L, result);
        verify(menuCategoryRepository).save(any(MenuCategory.class));
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 성공")
    void getCategories_Success() {
        // Given
        Long storeId = 1L;
        List<MenuCategory> categories = Arrays.asList(
                MenuCategory.builder().id(1L).storeId(storeId).name("메인 메뉴").displayOrder(1).build(),
                MenuCategory.builder().id(2L).storeId(storeId).name("사이드 메뉴").displayOrder(2).build()
        );

        when(menuCategoryRepository.findByStoreIdOrderByDisplayOrderAsc(storeId)).thenReturn(categories);

        // When
        List<MenuCategoryResponseDto> result = menuCategoryService.getCategories(storeId);

        // Then
        assertEquals(2, result.size());
        assertEquals("메인 메뉴", result.get(0).getName());
        assertEquals("사이드 메뉴", result.get(1).getName());
        verify(menuCategoryRepository).findByStoreIdOrderByDisplayOrderAsc(storeId);
    }

    @Test
    @DisplayName("카테고리 일괄 수정 - 성공")
    void updateCategories_Success() {
        // Given
        Long storeId = 1L;
        List<MenuCategoryUpdateRequestDto> requestDtos = Arrays.asList(updateRequestDto);
        List<MenuCategory> categories = Arrays.asList(
                MenuCategory.builder().id(1L).storeId(storeId).name("메인 메뉴").displayOrder(1).build()
        );

        when(menuCategoryRepository.findAllById(Arrays.asList(1L))).thenReturn(categories);

        // When
        List<Long> result = menuCategoryService.updateCategories(storeId, requestDtos);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0));
        verify(menuCategoryRepository).findAllById(Arrays.asList(1L));
    }

    @Test
    @DisplayName("카테고리 일괄 수정 - 카테고리 ID 빈 리스트")
    void updateCategories_EmptyIds() {
        // Given
        Long storeId = 1L;
        List<MenuCategoryUpdateRequestDto> requestDtos = Arrays.asList();

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuCategoryService.updateCategories(storeId, requestDtos));
        assertEquals(ErrorCode.CATEGORY_ID_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 일괄 수정 - 카테고리 없음")
    void updateCategories_CategoryNotFound() {
        // Given
        Long storeId = 1L;
        List<MenuCategoryUpdateRequestDto> requestDtos = Arrays.asList(updateRequestDto);

        when(menuCategoryRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuCategoryService.updateCategories(storeId, requestDtos));
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 일괄 수정 - 접근 권한 없음")
    void updateCategories_AccessDenied() {
        // Given
        Long storeId = 1L;
        Long wrongStoreId = 2L;
        List<MenuCategoryUpdateRequestDto> requestDtos = Arrays.asList(updateRequestDto);
        List<MenuCategory> categories = Arrays.asList(
                MenuCategory.builder().id(1L).storeId(wrongStoreId).name("메인 메뉴").displayOrder(1).build()
        );

        when(menuCategoryRepository.findAllById(Arrays.asList(1L))).thenReturn(categories);

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuCategoryService.updateCategories(storeId, requestDtos));
        assertEquals(ErrorCode.CATEGORY_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 삭제 - 성공")
    void deleteCategory_Success() {
        // Given
        Long storeId = 1L;
        Long categoryId = 1L;
        MenuCategory category = MenuCategory.builder()
                .id(categoryId)
                .storeId(storeId)
                .name("메인 메뉴")
                .displayOrder(1)
                .build();

        when(menuCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        menuCategoryService.deleteCategory(storeId, categoryId);

        // Then
        verify(menuCategoryRepository).findById(categoryId);
        verify(menuCategoryRepository).delete(category);
    }

    @Test
    @DisplayName("카테고리 삭제 - 카테고리 없음")
    void deleteCategory_CategoryNotFound() {
        // Given
        Long storeId = 1L;
        Long categoryId = 1L;

        when(menuCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuCategoryService.deleteCategory(storeId, categoryId));
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 삭제 - 접근 권한 없음")
    void deleteCategory_AccessDenied() {
        // Given
        Long storeId = 1L;
        Long wrongStoreId = 2L;
        Long categoryId = 1L;
        MenuCategory category = MenuCategory.builder()
                .id(categoryId)
                .storeId(wrongStoreId)
                .name("메인 메뉴")
                .displayOrder(1)
                .build();

        when(menuCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuCategoryService.deleteCategory(storeId, categoryId));
        assertEquals(ErrorCode.CATEGORY_ACCESS_DENIED, exception.getErrorCode());
    }
}