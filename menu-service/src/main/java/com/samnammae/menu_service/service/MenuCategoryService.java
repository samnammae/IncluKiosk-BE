package com.samnammae.menu_service.service;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.domain.menu.MenuRepository;
import com.samnammae.menu_service.domain.menucategory.MenuCategory;
import com.samnammae.menu_service.domain.menucategory.MenuCategoryRepository;
import com.samnammae.menu_service.dto.request.MenuCategoryCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuCategoryUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuCategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuCategoryService {

    private static final Logger log = LoggerFactory.getLogger(MenuCategoryService.class);

    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuRepository menuRepository;

    // 매장 접근 권한 검증
    public void validateStoreAccess(Long storeId, String managedStoreIds) {
        // --- 상세 로깅 추가 ---
        log.info("권한 검증 시작: storeId={}, managedStoreIds='{}'", storeId, managedStoreIds);

        // managedStoreIds가 비어있거나 null인 경우를 방어
        if (!StringUtils.hasText(managedStoreIds)) {
            log.error("권한 검증 실패: X-MANAGED-STORE-IDS 헤더가 비어있거나 null입니다.");
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        List<Long> accessibleStoreIds;
        try {
            // 쉼표로 구분된 문자열을 Long 리스트로 변환
            accessibleStoreIds = Arrays.stream(managedStoreIds.split(","))
                    .map(String::trim) // 각 ID의 앞뒤 공백 제거
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException e) {
            log.error("권한 검증 실패: managedStoreIds '{}' 파싱 중 오류 발생.", managedStoreIds, e);
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        log.debug("파싱된 접근 가능 매장 ID 목록: {}", accessibleStoreIds);

        if (!accessibleStoreIds.contains(storeId)) {
            log.warn("권한 검증 실패: 사용자(매장목록:{})는 storeId:{}에 접근할 수 없습니다.", accessibleStoreIds, storeId);
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        log.info("권한 검증 성공: storeId={}", storeId);
    }

    // 카테고리 생성
    @Transactional
    public Long createCategory(Long storeId, MenuCategoryCreateRequestDto requestDto) {
        MenuCategory menuCategory = MenuCategory.builder().
                storeId(storeId)
                .name(requestDto.getName())
                .displayOrder(requestDto.getDisplayOrder())
                .build();

        return menuCategoryRepository.save(menuCategory).getId();
    }

    // 카테고리 목록 조회
    public List<MenuCategoryResponseDto> getCategories(Long storeId) {
        List<MenuCategory> categories = menuCategoryRepository.findByStoreIdOrderByDisplayOrderAsc(storeId);

        return categories.stream()
                .map(category -> new MenuCategoryResponseDto(
                        category.getId(),
                        category.getName(),
                        category.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    // 카테고리 목록 일괄 수정
    @Transactional
    public List<Long> updateCategories(Long storeId, List<MenuCategoryUpdateRequestDto> requestDtos) {
        // DB에서 수정할 카테고리들을 한 번에 조회
        List<Long> categoryIds = requestDtos.stream().map(MenuCategoryUpdateRequestDto::getId).toList();

        // 카테고리 ID가 비어있으면 예외 처리
        if (categoryIds.isEmpty()) {
            throw new CustomException(ErrorCode.CATEGORY_ID_EMPTY);
        }

        // 카테고리 ID로 카테고리 엔티티를 조회하여 Map으로 변환
        Map<Long, MenuCategory> categoryMap = menuCategoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(MenuCategory::getId, Function.identity()));

        // DTO를 기반으로 엔티티 정보 업데이트
        requestDtos.forEach(dto -> {
            MenuCategory category = categoryMap.get(dto.getId());
            if (category != null && category.getStoreId().equals(storeId)) {
                category.update(dto.getName(), dto.getDisplayOrder());
            } else if (category == null) {
                throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
            } else {
                throw new CustomException(ErrorCode.CATEGORY_ACCESS_DENIED);
            }
        });

        return categoryIds;
    }

    // 카테고리 삭제
    @Transactional
    public void deleteCategory(Long storeId, Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode. CATEGORY_NOT_FOUND));

        // 소유권 확인
        if (!category.getStoreId().equals(storeId)) {
            throw new CustomException(ErrorCode.CATEGORY_ACCESS_DENIED);
        }

        // 카테고리에 속한 모든 메뉴 삭제
//        menuRepository.deleteByCategoryId(categoryId); // 주석 처리: 메뉴 삭제 로직 추후 추가 예정

        // 카테고리 삭제
        menuCategoryRepository.delete(category);
    }
}