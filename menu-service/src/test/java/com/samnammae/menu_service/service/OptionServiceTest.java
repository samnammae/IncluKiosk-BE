package com.samnammae.menu_service.service;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.domain.option.Option;
import com.samnammae.menu_service.domain.option.OptionRepository;
import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import com.samnammae.menu_service.domain.optioncategory.OptionCategoryRepository;
import com.samnammae.menu_service.domain.optioncategory.OptionCategoryType;
import com.samnammae.menu_service.dto.request.OptionCategoryRequestDto;
import com.samnammae.menu_service.dto.request.OptionRequestDto;
import com.samnammae.menu_service.dto.response.OptionCategoryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {

    private final Long storeId = 1L;
    private final Long categoryId = 1L;
    private final Long optionId = 101L;
    @Mock
    private OptionCategoryRepository optionCategoryRepository;
    @Mock
    private OptionRepository optionRepository;
    @InjectMocks
    private OptionService optionService;
    private OptionCategory testCategory;
    private OptionCategoryRequestDto requestDto;

    @BeforeEach
    void setUp() {
        testCategory = OptionCategory.builder()
                .id(categoryId)
                .storeId(storeId)
                .name("사이즈")
                .type(OptionCategoryType.SINGLE)
                .isRequired(true)
                .build();

        Option testOption = Option.builder()
                .id(optionId)
                .name("Regular")
                .price(0)
                .isDefault(true)
                .optionCategory(testCategory)
                .build();

        testCategory.getOptions().add(testOption);

        OptionRequestDto optionDto = new OptionRequestDto("Regular", 0, true);
        requestDto = new OptionCategoryRequestDto("사이즈", "SINGLE", true, List.of(optionDto));
    }

    @Test
    @DisplayName("매장 접근 권한 검증 - 성공")
    void validateStoreAccess_Success() {
        // Given
        String managedStoreIds = "1,2,3";

        // When & Then
        assertDoesNotThrow(() -> optionService.validateStoreAccess(storeId, managedStoreIds));
    }

    @Test
    @DisplayName("매장 접근 권한 검증 - 실패")
    void validateStoreAccess_Fail() {
        // Given
        Long wrongStoreId = 4L;
        String managedStoreIds = "1,2,3";

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> optionService.validateStoreAccess(wrongStoreId, managedStoreIds));
        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("옵션 카테고리 생성 - 성공")
    void createOptionCategory_Success() {
        // Given
        when(optionCategoryRepository.existsByStoreIdAndName(storeId, requestDto.getName())).thenReturn(false);
        when(optionCategoryRepository.save(any(OptionCategory.class))).thenReturn(testCategory);

        // When
        Long result = optionService.createOptionCategory(storeId, requestDto);

        // Then
        assertEquals(categoryId, result);
        verify(optionCategoryRepository).save(any(OptionCategory.class));
    }

    @Test
    @DisplayName("옵션 카테고리 생성 - 이름 중복 실패")
    void createOptionCategory_NameDuplicated_Fail() {
        // Given
        when(optionCategoryRepository.existsByStoreIdAndName(storeId, requestDto.getName())).thenReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> optionService.createOptionCategory(storeId, requestDto));
        assertEquals(ErrorCode.OPTION_CATEGORY_NAME_DUPLICATED, exception.getErrorCode());
    }

    @Test
    @DisplayName("옵션 목록 조회 - 성공")
    void getOptionsByStore_Success() {
        // Given
        when(optionCategoryRepository.findAllByStoreIdWithDetails(storeId)).thenReturn(List.of(testCategory));

        // When
        List<OptionCategoryResponseDto> result = optionService.getOptionsByStore(storeId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("사이즈");
        assertThat(result.get(0).getOptions()).hasSize(1);
        verify(optionCategoryRepository).findAllByStoreIdWithDetails(storeId);
    }

    @Test
    @DisplayName("옵션 카테고리 수정 - 성공")
    void updateOptionCategory_Success() {
        // Given
        when(optionCategoryRepository.findByIdAndStoreId(categoryId, storeId)).thenReturn(Optional.of(testCategory));

        // When
        Long result = optionService.updateOptionCategory(storeId, categoryId, requestDto);

        // Then
        assertEquals(categoryId, result);
        verify(optionCategoryRepository).findByIdAndStoreId(categoryId, storeId);
    }

    @Test
    @DisplayName("옵션 카테고리 수정 - 찾을 수 없음")
    void updateOptionCategory_NotFound() {
        // Given
        when(optionCategoryRepository.findByIdAndStoreId(categoryId, storeId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> optionService.updateOptionCategory(storeId, categoryId, requestDto));
        assertEquals(ErrorCode.OPTION_CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("옵션 카테고리 삭제 - 성공")
    void deleteOptionCategory_Success() {
        // Given
        when(optionCategoryRepository.findByIdAndStoreId(categoryId, storeId)).thenReturn(Optional.of(testCategory));
        doNothing().when(optionCategoryRepository).delete(testCategory);

        // When
        optionService.deleteOptionCategory(storeId, categoryId);

        // Then
        verify(optionCategoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("옵션 카테고리 삭제 - 찾을 수 없음")
    void deleteOptionCategory_NotFound() {
        // Given
        when(optionCategoryRepository.findByIdAndStoreId(categoryId, storeId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> optionService.deleteOptionCategory(storeId, categoryId));
        assertEquals(ErrorCode.OPTION_CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("개별 옵션 삭제 - 성공")
    void deleteOption_Success() {
        // Given
        when(optionCategoryRepository.findByIdAndStoreId(categoryId, storeId)).thenReturn(Optional.of(testCategory));
        doNothing().when(optionRepository).deleteById(optionId);

        // When
        optionService.deleteOption(storeId, categoryId, optionId);

        // Then
        verify(optionRepository).deleteById(optionId);
    }

    @Test
    @DisplayName("개별 옵션 삭제 - 옵션 없음")
    void deleteOption_OptionNotFound() {
        // Given
        Long wrongOptionId = 999L;
        when(optionCategoryRepository.findByIdAndStoreId(categoryId, storeId)).thenReturn(Optional.of(testCategory));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> optionService.deleteOption(storeId, categoryId, wrongOptionId));
        assertEquals(ErrorCode.OPTION_NOT_FOUND, exception.getErrorCode());
    }
}