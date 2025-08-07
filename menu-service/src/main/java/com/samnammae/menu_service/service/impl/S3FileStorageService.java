package com.samnammae.menu_service.service.impl;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.menu_service.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    private static final String FOLDER_PREFIX = "back-end/menu/";

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new CustomException(ErrorCode.INVALID_FILE_NAME);
        }

        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        String fullKey = FOLDER_PREFIX + uniqueFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fullKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fullKey);

        } catch (IOException e) {
            log.error("S3 파일 업로드 중 IO 오류 발생: {}", originalFilename, e);
            throw new CustomException(ErrorCode.FILE_IO_ERROR);
        } catch (S3Exception e) {
            log.error("S3 서비스 오류 발생: {}", originalFilename, e);
            throw new CustomException(ErrorCode.S3_SERVICE_ERROR);
        } catch (Exception e) {
            log.error("파일 업로드 중 예상치 못한 오류: {}", originalFilename, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // 1. URL에서 파일 키(이름) 추출
            URL url = new URL(fileUrl);
            String fileKey = url.getPath().substring(1); // URL 경로에서 첫 '/' 제거

            // 2. S3에서 파일 삭제
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 성공: {}", fileKey);

        } catch (S3Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: {}", fileUrl, e);
        } catch (Exception e) {
            log.error("파일 삭제 중 일반 오류 발생: {}", fileUrl, e);
        }
    }
}