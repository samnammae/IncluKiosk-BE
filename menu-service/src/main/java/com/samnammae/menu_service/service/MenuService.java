package com.samnammae.menu_service.service;

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
import com.samnammae.menu_service.dto.request.MenuCreateRequestDto;
import com.samnammae.menu_service.dto.request.MenuUpdateRequestDto;
import com.samnammae.menu_service.dto.response.MenuListResponseDto;
import com.samnammae.menu_service.dto.response.MenuResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final OptionCategoryRepository optionCategoryRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    // 매장 접근 권한 검증
    public void validateStoreAccess(Long storeId, String managedStoreIds) {
        List<Long> accessibleStoreIds = Arrays.stream(managedStoreIds.split(","))
                .map(Long::parseLong)
                .toList();

        if (!accessibleStoreIds.contains(storeId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }
    }

    @Transactional
    public Long createMenu(Long storeId, MenuCreateRequestDto requestDto, MultipartFile image) {
        // 1. 연관 엔티티 조회
        MenuCategory menuCategory = menuCategoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        Set<OptionCategory> optionCategories = findOptionCategoriesByIds(requestDto.getOptionCategoryIds());

        // 2. 이미지 저장
        String imageUrl = (image != null && !image.isEmpty()) ? fileStorageService.storeFile(image) : null;

        // 3. 메뉴 엔티티 생성 및 저장
        Menu menu = Menu.builder()
                .storeId(storeId)
                .name(requestDto.getName())
                .price(requestDto.getPrice())
                .description(requestDto.getDescription())
                .imageUrl(imageUrl)
                .isSoldOut(requestDto.isSoldOut())
                .menuCategory(menuCategory)
                .optionCategories(optionCategories)
                .build();

        Menu savedMenu = menuRepository.save(menu);

        return savedMenu.getId();
    }

    public MenuListResponseDto getMenusByStore(Long storeId) {
        List<Menu> allMenus = menuRepository.findAllByStoreIdWithDetails(storeId);

        Map<String, List<MenuResponseDto>> menusByCategory = allMenus.stream()
                .collect(Collectors.groupingBy(
                        menu -> menu.getMenuCategory().getName(),
                        Collectors.mapping(MenuResponseDto::from, Collectors.toList())
                ));

        List<String> categories = menusByCategory.keySet().stream().sorted().toList();

        return new MenuListResponseDto(categories, menusByCategory);
    }

    @Transactional
    public Long updateMenu(Long storeId, Long menuId, MenuUpdateRequestDto requestDto, MultipartFile image) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 1. 소유권 확인
        if (!menu.getStoreId().equals(storeId)) {
            throw new CustomException(ErrorCode.MENU_STORE_MISMATCH);
        }

        // 2. 이미지 처리 (새 이미지가 있으면 기존 이미지 삭제 후 저장)
        String imageUrl = menu.getImageUrl();
        if (image != null && !image.isEmpty()) {
            if (StringUtils.hasText(imageUrl)) {
                fileStorageService.deleteFile(imageUrl);
            }
            imageUrl = fileStorageService.storeFile(image);
        }

        // 3. 연관 엔티티 조회 및 업데이트
        MenuCategory menuCategory = menuCategoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
        Set<OptionCategory> optionCategories = findOptionCategoriesByIds(requestDto.getOptionCategoryIds());

        // 4. 엔티티 업데이트 (Dirty Checking 활용)
        menu.update(
                requestDto.getName(),
                requestDto.getPrice(),
                requestDto.getDescription(),
                imageUrl,
                requestDto.isSoldOut(),
                menuCategory,
                optionCategories
        );

        return menu.getId();
    }

    @Transactional
    public void deleteMenu(Long storeId, Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        if (!menu.getStoreId().equals(storeId)) {
            throw new CustomException(ErrorCode.MENU_STORE_MISMATCH);
        }

        // 이미지 파일이 있으면 삭제
        if (StringUtils.hasText(menu.getImageUrl())) {
            fileStorageService.deleteFile(menu.getImageUrl());
        }

        menuRepository.delete(menu);
    }

    // JSON 문자열을 파싱하여 OptionCategory Set을 반환하는 헬퍼 메서드
    private Set<OptionCategory> findOptionCategoriesByIds(String optionCategoryIdsJson) {
        if (!StringUtils.hasText(optionCategoryIdsJson)) {
            return new HashSet<>();
        }

        try {
            List<Long> ids = objectMapper.readValue(optionCategoryIdsJson, new TypeReference<>() {
            });
            return new HashSet<>(optionCategoryRepository.findAllById(ids));
        } catch (IOException e) {
            log.error("옵션 카테고리 ID JSON 파싱 오류", e);
            throw new CustomException(ErrorCode.INVALID_OPTION_CATEGORY_FORMAT);
        }
    }
}