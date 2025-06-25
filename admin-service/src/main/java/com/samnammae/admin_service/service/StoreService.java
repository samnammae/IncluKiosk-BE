package com.samnammae.admin_service.service;

import com.samnammae.admin_service.domain.store.Store;
import com.samnammae.admin_service.domain.store.StoreRepository;
import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

}
