package com.samnammae.admin_service.service;

import com.samnammae.admin_service.domain.store.Store;
import com.samnammae.admin_service.domain.store.StoreRepository;
import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.dto.response.StoreResponse;
import com.samnammae.admin_service.dto.response.StoreSimpleResponse;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final FileStorageService fileStorageService;

    public StoreService(StoreRepository storeRepository,
                        @Qualifier("s3FileStorage") FileStorageService fileStorageService) {
        this.storeRepository = storeRepository;
        this.fileStorageService = fileStorageService;
    }

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
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getOwnerId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 기존 파일 URL 백업
        String oldMainImg = store.getMainImgUrl();
        String oldLogoImg = store.getLogoImgUrl();
        String oldBackground = store.getStartBackgroundUrl();

        // 새 파일 업로드 (파일이 제공된 경우만)
        String newMainImg = request.getMainImg() != null && !request.getMainImg().isEmpty()
                ? uploadFile(request.getMainImg()) : oldMainImg;
        String newLogoImg = request.getLogoImg() != null && !request.getLogoImg().isEmpty()
                ? uploadFile(request.getLogoImg()) : oldLogoImg;
        String newBackground = request.getStartBackground() != null && !request.getStartBackground().isEmpty()
                ? uploadFile(request.getStartBackground()) : oldBackground;

        // 매장 정보 업데이트
        store.update(
                request.getName(),
                request.getPhone(),
                request.getAddress(),
                request.getIntroduction(),
                newMainImg,
                newLogoImg,
                newBackground,
                request.getMainColor(),
                request.getSubColor(),
                request.getTextColor()
        );

        Store savedStore = storeRepository.save(store);

        // 기존 파일 삭제 (새 파일로 변경된 경우만)
        if (oldMainImg != null && !oldMainImg.equals(newMainImg)) {
            fileStorageService.delete(oldMainImg);
        }
        if (oldLogoImg != null && !oldLogoImg.equals(newLogoImg)) {
            fileStorageService.delete(oldLogoImg);
        }
        if (oldBackground != null && !oldBackground.equals(newBackground)) {
            fileStorageService.delete(oldBackground);
        }

        return StoreResponse.from(savedStore);
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
