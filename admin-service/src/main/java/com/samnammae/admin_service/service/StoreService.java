package com.samnammae.admin_service.service;

import com.samnammae.admin_service.domain.store.Store;
import com.samnammae.admin_service.domain.store.StoreRepository;
import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.dto.response.StoreResponse;
import com.samnammae.admin_service.dto.response.StoreSimpleResponse;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final FileStorageService fileStorageService;

    // 가게 등록
    public Long createStore(Long ownerId, StoreRequest request) {
        String mainImgUrl = uploadFile(request.getMainImg());
        String logoImgUrl = uploadFile(request.getLogoImg());
        String backgroundUrl = uploadFile(request.getStartBackground());

        Store store = Store.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .introduction(request.getIntroduction())
                .mainImgUrl(mainImgUrl)
                .logoImgUrl(logoImgUrl)
                .startBackgroundUrl(backgroundUrl)
                .mainColor(request.getMainColor())
                .subColor(request.getSubColor())
                .textColor(request.getTextColor())
                .build();

        return storeRepository.save(store).getId();
    }

    // 이미지 파일 업로드
    private String uploadFile(MultipartFile file) {
        // 파일이 비어있거나 null인 경우 예외 처리
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 업로드 서비스 호출
        try {
            return fileStorageService.upload(file);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 매장 목록 조회
    public List<StoreSimpleResponse> getStoreList(Long userId) {
        // Store 객체를 StoreSimpleResponse로 변환
        return storeRepository.findAllByOwnerId(userId).stream()
                .map(StoreSimpleResponse::from)
                .toList();
    }

    // 특정 매장 조회
    public StoreResponse getStore(Long userId, Long storeId) {
        // 매장 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 매장 소유자 확인
        if (!store.getOwnerId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        return StoreResponse.from(store);
    }

    // 매장 정보 수정
    public StoreResponse updateStore(Long userId, Long storeId, StoreRequest request) {
        // 매장 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 매장 소유자 확인
        if (!store.getOwnerId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 파일 업로드
        String mainImgUrl = uploadFile(request.getMainImg());
        String logoImgUrl = uploadFile(request.getLogoImg());
        String backgroundUrl = uploadFile(request.getStartBackground());

        // 매장 정보 업데이트
        store.update(
                request.getName(),
                request.getPhone(),
                request.getAddress(),
                request.getIntroduction(),
                mainImgUrl,
                logoImgUrl,
                backgroundUrl,
                request.getMainColor(),
                request.getSubColor(),
                request.getTextColor()
        );

        return StoreResponse.from(storeRepository.save(store));
    }

    // 매장 삭제
    @Transactional
    public void deleteStore(Long userId, Long storeId) {
        // 매장 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 매장 소유자 확인
        if (!store.getOwnerId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 파일 삭제
        List<String> filesToDelete = new ArrayList<>();
        if (store.getMainImgUrl() != null) filesToDelete.add(store.getMainImgUrl());
        if (store.getLogoImgUrl() != null) filesToDelete.add(store.getLogoImgUrl());
        if (store.getStartBackgroundUrl() != null) filesToDelete.add(store.getStartBackgroundUrl());

        filesToDelete.forEach(fileStorageService::delete);

        // 매장 삭제
        storeRepository.delete(store);
    }
}
