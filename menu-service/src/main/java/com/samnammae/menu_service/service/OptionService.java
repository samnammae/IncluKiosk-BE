package com.samnammae.menu_service.service;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.domain.option.Option;
import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import com.samnammae.menu_service.domain.optioncategory.OptionCategoryRepository;
import com.samnammae.menu_service.domain.optioncategory.OptionCategoryType;
import com.samnammae.menu_service.dto.request.OptionCategoryRequestDto;
import com.samnammae.menu_service.dto.request.OptionRequestDto;
import com.samnammae.menu_service.dto.response.OptionCategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionService {

    private final OptionCategoryRepository optionCategoryRepository;

    public void validateStoreAccess(Long storeId, String managedStoreIds) {
        List<Long> accessibleStoreIds = Arrays.stream(managedStoreIds.split(",")).map(Long::parseLong).toList();
        if (!accessibleStoreIds.contains(storeId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }
    }

    @Transactional
    public Long createOptionCategory(Long storeId, OptionCategoryRequestDto requestDto) {
        // 옵션 카테고리 이름 중복 검증
        if (optionCategoryRepository.existsByStoreIdAndName(storeId, requestDto.getName())) {
            throw new CustomException(ErrorCode.OPTION_CATEGORY_NAME_DUPLICATED);
        }

        // 옵션 카테고리 생성
        OptionCategory category = OptionCategory.builder()
                .storeId(storeId)
                .name(requestDto.getName())
                .type(OptionCategoryType.valueOf(requestDto.getType()))
                .isRequired(requestDto.isRequired())
                .build();

        // 옵션 카테고리에 속하는 옵션들을 생성
        List<Option> options = requestDto.getOptions().stream()
                .map(optionDto -> buildOption(optionDto, category))
                .toList();

        // 옵션 카테고리에 옵션 추가
        category.getOptions().addAll(options);

        // 옵션 카테고리 저장
        OptionCategory savedCategory = optionCategoryRepository.save(category);
        return savedCategory.getId();
    }

    // 매장별 옵션 카테고리 조회
    public List<OptionCategoryResponseDto> getOptionsByStore(Long storeId) {
        return optionCategoryRepository.findAllByStoreIdWithDetails(storeId).stream()
                .map(OptionCategoryResponseDto::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public Long updateOptionCategory(Long storeId, Long optionCategoryId, OptionCategoryRequestDto requestDto) {
        // 옵션 카테고리 조회
        OptionCategory category = optionCategoryRepository.findByIdAndStoreId(optionCategoryId, storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_CATEGORY_NOT_FOUND));

        // 이름이 변경되는 경우 중복 검증 필요
        if (!category.getName().equals(requestDto.getName()) &&
                optionCategoryRepository.existsByStoreIdAndName(storeId, requestDto.getName())) {
            throw new CustomException(ErrorCode.OPTION_CATEGORY_NAME_DUPLICATED);
        }

        category.update(
                requestDto.getName(),
                OptionCategoryType.valueOf(requestDto.getType()),
                requestDto.isRequired()
        );

        // 옵션 카테고리 정보 업데이트
        category.getOptions().clear();
        optionCategoryRepository.flush();

        List<Option> newOptions = requestDto.getOptions().stream()
                .map(optionDto -> buildOption(optionDto, category))
                .toList();
        category.getOptions().addAll(newOptions);

        return category.getId();
    }

    @Transactional
    public void deleteOptionCategory(Long storeId, Long optionCategoryId) {
        OptionCategory category = optionCategoryRepository.findByIdAndStoreId(optionCategoryId, storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_CATEGORY_NOT_FOUND));
        optionCategoryRepository.delete(category);
    }


    @Transactional
    public void deleteOption(Long storeId, Long optionCategoryId, Long optionId) {
        // 옵션 카테고리 조회
        OptionCategory category = optionCategoryRepository.findByIdAndStoreId(optionCategoryId, storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_CATEGORY_NOT_FOUND));


        // 옵션 ID로 개별 옵션 조회
        Option optionToDelete = category.getOptions().stream()
                .filter(opt -> opt.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

        // 옵션 삭제(JPA의 orphanRemoval을 사용하여 자동으로 삭제됨)
        category.getOptions().remove(optionToDelete);
    }

    private Option buildOption(OptionRequestDto dto, OptionCategory category) {
        return Option.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .isDefault(dto.isDefault())
                .optionCategory(category)
                .build();
    }
}