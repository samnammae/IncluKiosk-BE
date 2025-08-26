package com.samnammae.admin_service.service.impl;

import com.samnammae.admin_service.service.FileStorageService;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@Qualifier("s3FileStorage")
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    private static final String ADMIN_FOLDER = "back-end/admin/";

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String fileName = UUID.randomUUID() + extension;
        String key = ADMIN_FOLDER + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("S3 파일 업로드 완료: {}", key);

            // 공개 URL 반환
            return getPublicUrl(key);

        } catch (IOException e) {
            log.error("파일 읽기에 실패했습니다. 파일명: {}", originalFilename, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (S3Exception e) {
            log.error("S3 파일 업로드에 실패했습니다. 파일명: {}", originalFilename, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            log.info("삭제할 파일 이름이 제공되지 않았습니다.");
            return;
        }

        // URL에서 키값 추출
        String key = extractKeyFromUrl(fileName);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 성공: {}", key);

        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: {}", key, e);
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    // 공개 URL 생성
    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    // URL에서 키값 추출 (삭제 시 사용)
    private String extractKeyFromUrl(String url) {
        if (url.startsWith("http")) {
            // URL인 경우 키값만 추출
            String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
            return url.replace(baseUrl, "");
        }
        // 이미 키값인 경우 그대로 반환
        return url;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}