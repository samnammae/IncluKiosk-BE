package com.samnammae.admin_service.service.impl;

import com.samnammae.admin_service.service.FileStorageService;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    @Override
    public String upload(MultipartFile file) {
        // 파일이 비어있는 경우 null을 반환하거나 예외 처리를 할 수 있습니다.
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String savedFileName = UUID.randomUUID() + extension;

        File targetFile = new File(uploadDir, savedFileName);

        try {
            // 업로드 디렉토리가 없으면 생성합니다.
            targetFile.getParentFile().mkdirs();
            file.transferTo(targetFile);
            log.info("파일 저장 완료: {}", targetFile.getAbsolutePath());

            // 데이터베이스에 저장할 순수 파일 이름만 반환합니다.
            return savedFileName;
        } catch (IOException e) {
            log.error("파일 저장에 실패했습니다. 파일명: {}", originalFilename, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }


    @Override
    public void delete(String fileName) {
        // fileName이 null이거나 비어있으면 아무 작업도 하지 않고 종료합니다. (Null-safe)
        if (!StringUtils.hasText(fileName)) {
            log.info("삭제할 파일 이름이 제공되지 않았습니다.");
            return;
        }

        File fileToDelete = new File(uploadDir, fileName);

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                log.info("파일 삭제 성공: {}", fileToDelete.getAbsolutePath());
            } else {
                // 파일은 존재하지만 삭제에 실패한 경우 (예: 권한 문제)
                log.error("파일 삭제 실패: {}", fileToDelete.getAbsolutePath());
                throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
            }
        } else {
            // 삭제하려는 파일이 애초에 존재하지 않는 경우

            log.warn("삭제할 파일이 존재하지 않습니다: {}", fileToDelete.getAbsolutePath());
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
