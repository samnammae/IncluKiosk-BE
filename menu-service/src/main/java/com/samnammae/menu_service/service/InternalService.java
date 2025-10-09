package com.samnammae.menu_service.service;

import com.samnammae.menu_service.domain.menu.Menu;
import com.samnammae.menu_service.domain.menu.MenuRepository;
import com.samnammae.menu_service.dto.response.MenuWithOptionDetailDto;
import com.samnammae.menu_service.dto.response.MenuWithOptionsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalService {

    private final MenuRepository menuRepository;

    public MenuWithOptionsResponseDto getMenusWithOptions(Long storeId) {
        // 매장의 모든 메뉴와 옵션 정보를 한 번에 조회 (N+1 방지를 위해 fetch join 사용)
        List<Menu> menus = menuRepository.findAllByStoreIdWithOptionCategories(storeId);

        // 메뉴를 카테고리별로 그룹핑
        Map<String, List<MenuWithOptionDetailDto>> menusByCategory = menus.stream()
                .collect(Collectors.groupingBy(
                        menu -> menu.getMenuCategory().getName(), // category -> menuCategory.getName()으로 변경
                        Collectors.mapping(
                                MenuWithOptionDetailDto::from,
                                Collectors.toList()
                        )
                ));

        // 카테고리 목록 추출 (displayOrder 순서로 정렬)
        List<String> categories = menus.stream()
                .map(menu -> menu.getMenuCategory().getName())
                .distinct()
                .sorted()
                .toList();

        return new MenuWithOptionsResponseDto(categories, menusByCategory);
    }
}