package com.samnammae.menu_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 파일을 저장하고 접근 가능한 URL을 반환합니다.
     * @param file 저장할 파일
     * @return 저장된 파일의 URL
     */
    String storeFile(MultipartFile file);

    /**
     * 기존 파일을 삭제합니다.
     * @param fileUrl 삭제할 파일의 URL
     */
    void deleteFile(String fileUrl);
}
