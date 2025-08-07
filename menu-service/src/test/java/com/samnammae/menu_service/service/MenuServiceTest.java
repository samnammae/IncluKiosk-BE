package com.samnammae.menu_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.domain.menu.Menu;
import com.samnammae.menu_service.domain.menu.MenuRepository;
import com.samnammae.menu_service.domain.menucategory.MenuCategory;
import com.samnammae.menu_service.domain.menucategory.MenuCategoryRepository;
import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import com.samnammae.menu_service.domain.optioncategory.OptionCategoryRepository;
import com.samnammae.menu_service.domain.optioncategory.OptionCategoryType;
import com.samnammae.menu_service.dto.request.MenuCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuListResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @Mock
    private OptionCategoryRepository optionCategoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MenuService menuService;

    private MenuCategory testCategory;
    private OptionCategory testOptionCategory;
    private Menu testMenu;
    private MenuCreateRequestDto createRequestDto;
    private MenuUpdateRequestDto updateRequestDto;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        testCategory = MenuCategory.builder()
                .id(1L)
                .storeId(1L)
                .name("메인 메뉴")
                .displayOrder(1)
                .build();

        testOptionCategory = OptionCategory.builder()
                .id(1L)
                .storeId(1L)
                .name("사이즈")
                .type(OptionCategoryType.SINGLE_CHOICE)
                .isRequired(true)
                .build();

        testMenu = Menu.builder()
                .id(1L)
                .storeId(1L)
                .name("치킨버거")
                .price(8000)
                .description("맛있는 치킨버거")
                .imageUrl("https://example.com/image.jpg")
                .isSoldOut(false)
                .menuCategory(testCategory)
                .optionCategories(Set.of(testOptionCategory))
                .build();

        createRequestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "[1]", false);

        updateRequestDto = new MenuUpdateRequestDto(
                "수정된 치킨버거", 9000, "더 맛있는 치킨버거", 1L, "[1]", false);

        testImage = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test content".getBytes());
    }

    @Test
    @DisplayName("매장 접근 권한 검증 - 성공")
    void validateStoreAccess_Success() {
        // Given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";

        // When & Then
        assertDoesNotThrow(() -> menuService.validateStoreAccess(storeId, managedStoreIds));
    }

    @Test
    @DisplayName("매장 접근 권한 검증 - 실패")
    void validateStoreAccess_Fail() {
        // Given
        Long storeId = 4L;
        String managedStoreIds = "1,2,3";

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.validateStoreAccess(storeId, managedStoreIds));
        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 생성 - 성공 (이미지 있음)")
    void createMenu_WithImage_Success() throws Exception {
        // Given
        Long storeId = 1L;
        String imageUrl = "https://example.com/image.jpg";

        setupMenuCreationMocks();
        when(fileStorageService.storeFile(testImage)).thenReturn(imageUrl);

        // When
        Long result = menuService.createMenu(storeId, createRequestDto, testImage);

        // Then
        assertEquals(1L, result);
        verifyMenuCreationCalls();
        verify(fileStorageService).storeFile(testImage);
    }

    @Test
    @DisplayName("메뉴 생성 - 성공 (이미지 없음)")
    void createMenu_WithoutImage_Success() throws Exception {
        // Given
        Long storeId = 1L;
        setupMenuCreationMocks();

        // When
        Long result = menuService.createMenu(storeId, createRequestDto, null);

        // Then
        assertEquals(1L, result);
        verifyMenuCreationCalls();
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    @DisplayName("메뉴 생성 - 카테고리 없음")
    void createMenu_CategoryNotFound() throws Exception {
        // Given
        Long storeId = 1L;
        // ObjectMapper 모킹 제거 - 카테고리 검증에서 실패하므로 불필요
        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.createMenu(storeId, createRequestDto, testImage));
        assertEquals(ErrorCode.MENU_CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 목록 조회 - 성공")
    void getMenusByStore_Success() {
        // Given
        Long storeId = 1L;
        List<Menu> menus = Arrays.asList(testMenu);
        when(menuRepository.findAllByStoreIdWithDetails(storeId)).thenReturn(menus);

        // When
        MenuListResponseDto result = menuService.getMenusByStore(storeId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCategories().size());
        assertTrue(result.getMenusByCategory().containsKey("메인 메뉴"));
        verify(menuRepository).findAllByStoreIdWithDetails(storeId);
    }

    @Test
    @DisplayName("메뉴 수정 - 성공")
    void updateMenu_Success() throws Exception {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;
        String newImageUrl = "https://example.com/new-image.jpg";

        setupMenuUpdateMocks(menuId);
        when(fileStorageService.storeFile(testImage)).thenReturn(newImageUrl);

        // When
        Long result = menuService.updateMenu(storeId, menuId, updateRequestDto, testImage);

        // Then
        assertEquals(1L, result);
        verify(menuRepository).findById(menuId);
        // 기존 이미지 URL로 삭제 검증 (testMenu.getImageUrl() = "https://example.com/image.jpg")
        verify(fileStorageService).deleteFile("https://example.com/image.jpg");
        verify(fileStorageService).storeFile(testImage);
    }

    @Test
    @DisplayName("메뉴 수정 - 메뉴 없음")
    void updateMenu_MenuNotFound() {
        // Given
        Long storeId = 1L;
        Long menuId = 999L;
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.updateMenu(storeId, menuId, updateRequestDto, testImage));
        assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 수정 - 매장 불일치")
    void updateMenu_StoreMismatch() {
        // Given
        Long wrongStoreId = 2L;
        Long menuId = 1L;
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.updateMenu(wrongStoreId, menuId, updateRequestDto, testImage));
        assertEquals(ErrorCode.MENU_STORE_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 삭제 - 성공")
    void deleteMenu_Success() {
        // Given
        Long storeId = 1L;
        Long menuId = 1L;
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

        // When
        menuService.deleteMenu(storeId, menuId);

        // Then
        verify(menuRepository).findById(menuId);
        verify(fileStorageService).deleteFile(testMenu.getImageUrl());
        verify(menuRepository).delete(testMenu);
    }

    @Test
    @DisplayName("메뉴 삭제 - 메뉴 없음")
    void deleteMenu_MenuNotFound() {
        // Given
        Long storeId = 1L;
        Long menuId = 999L;
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.deleteMenu(storeId, menuId));
        assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 삭제 - 매장 불일치")
    void deleteMenu_StoreMismatch() {
        // Given
        Long wrongStoreId = 2L;
        Long menuId = 1L;
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.deleteMenu(wrongStoreId, menuId));
        assertEquals(ErrorCode.MENU_STORE_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("옵션 카테고리 JSON 파싱 오류")
    void createMenu_InvalidOptionCategoryFormat() throws Exception {
        // Given
        Long storeId = 1L;
        MenuCreateRequestDto invalidRequestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "invalid-json", false);

        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        // JsonProcessingException으로 변경 (RuntimeException 상속)
        when(objectMapper.readValue(eq("invalid-json"), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("JSON 파싱 오류") {});

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.createMenu(storeId, invalidRequestDto, testImage));
        assertEquals(ErrorCode.INVALID_OPTION_CATEGORY_FORMAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 생성 - 빈 옵션 카테고리")
    void createMenu_EmptyOptionCategories() throws Exception {
        // Given
        Long storeId = 1L;
        MenuCreateRequestDto requestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, "", false);

        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);

        // When
        Long result = menuService.createMenu(storeId, requestDto, null);

        // Then
        assertEquals(1L, result);
        verify(optionCategoryRepository, never()).findAllById(any());
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    @DisplayName("메뉴 생성 - null 옵션 카테고리")
    void createMenu_NullOptionCategories() throws Exception {
        // Given
        Long storeId = 1L;
        MenuCreateRequestDto requestDto = new MenuCreateRequestDto(
                "치킨버거", 8000, "맛있는 치킨버거", 1L, null, false);

        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);

        // When
        Long result = menuService.createMenu(storeId, requestDto, null);

        // Then
        assertEquals(1L, result);
        verify(optionCategoryRepository, never()).findAllById(any());
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    // 헬퍼 메서드들
    private void setupObjectMapperMock() throws Exception {
        when(objectMapper.readValue(eq("[1]"), any(TypeReference.class)))
                .thenReturn(Arrays.asList(1L));
    }

    private void setupMenuCreationMocks() throws Exception {
        setupObjectMapperMock();
        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(optionCategoryRepository.findAllById(Arrays.asList(1L)))
                .thenReturn(Arrays.asList(testOptionCategory));
        when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);
    }

    private void setupMenuUpdateMocks(Long menuId) throws Exception {
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));
        setupObjectMapperMock();
        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(optionCategoryRepository.findAllById(Arrays.asList(1L)))
                .thenReturn(Arrays.asList(testOptionCategory));
    }

    private void verifyMenuCreationCalls() throws Exception {
        verify(menuCategoryRepository).findById(1L);
        verify(optionCategoryRepository).findAllById(Arrays.asList(1L));
        verify(menuRepository).save(any(Menu.class));
        verify(objectMapper).readValue(eq("[1]"), any(TypeReference.class));
    }
}