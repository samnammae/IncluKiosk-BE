package com.samnammae.admin_service.service.impl;

import com.samnammae.admin_service.service.FileStorageService;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@Qualifier("localFileStorage")
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String savedFileName = UUID.randomUUID() + extension;

        File targetFile = new File(uploadDir, savedFileName);

        try {
            file.transferTo(targetFile);
            log.info("파일 저장 완료: {}", targetFile.getAbsolutePath());
            return "/files/" + savedFileName; // 정적 리소스로 접근 가능하게 리턴 경로 설정
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
