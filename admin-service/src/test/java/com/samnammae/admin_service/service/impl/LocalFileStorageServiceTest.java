package com.samnammae.admin_service.service.impl;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class LocalFileStorageServiceTest {

    private LocalFileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService();
        // ReflectionTestUtils를 사용하여 private 필드에 값 주입
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("파일 업로드 성공 테스트")
    void upload_success() {
        // given
        String fileName = "test-file.jpg";
        String contentType = "image/jpeg";
        byte[] content = "test image content".getBytes();
        MultipartFile file = new MockMultipartFile("file", fileName, contentType, content);

        // when
        String savedPath = fileStorageService.upload(file);

        // then
        assertThat(savedPath).isNotNull();
        assertThat(savedPath).startsWith("/files/");

        // 파일이 실제로 저장되었는지 확인
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotNull();
        assertThat(files).hasSize(1);

        String uuid = savedPath.substring(7); // "/files/" 제외
        assertThat(files[0].getName()).isEqualTo(uuid);

        // 파일 내용 확인
        try {
            byte[] savedContent = Files.readAllBytes(files[0].toPath());
            assertThat(savedContent).isEqualTo(content);
        } catch (IOException e) {
            fail("파일을 읽는 중 예외 발생", e);
        }
    }

    @Test
    @DisplayName("확장자가 없는 파일 업로드 테스트")
    void upload_fileWithoutExtension() {
        // given
        String fileName = "test-file-without-extension";
        String contentType = "application/octet-stream";
        byte[] content = "test content without extension".getBytes();
        MultipartFile file = new MockMultipartFile("file", fileName, contentType, content);

        // when
        String savedPath = fileStorageService.upload(file);

        // then
        assertThat(savedPath).isNotNull();

        // 파일이 실제로 저장되었는지 확인
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotNull();
        assertThat(files).hasSize(1);
    }

    @Test
    @DisplayName("null 파일명 처리 테스트")
    void upload_nullFilename() {
        // given
        String contentType = "application/octet-stream";
        byte[] content = "test content with null filename".getBytes();
        MultipartFile file = new MockMultipartFile("file", null, contentType, content);

        // when
        String savedPath = fileStorageService.upload(file);

        // then
        assertThat(savedPath).isNotNull();

        // 파일이 실제로 저장되었는지 확인
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotNull();
        assertThat(files).hasSize(1);
    }

    @Test
    @DisplayName("파일 업로드 중 IOException 발생 시 CustomException 반환")
    void upload_fail_dueToIOException() throws IOException {
        // given
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.png");
        Mockito.doThrow(new IOException("disk full")).when(mockFile).transferTo(Mockito.any(File.class));

        // when and then
        assertThatThrownBy(() -> fileStorageService.upload(mockFile))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.FILE_UPLOAD_FAILED);
                });
    }

    @Test
    @DisplayName("getExtension 메소드 테스트 - 정상 파일명")
    void getExtension_normalFilename() {
        // given
        String filename = "test.jpg";

        // when
        String extension = ReflectionTestUtils.invokeMethod(fileStorageService, "getExtension", filename);

        // then
        assertThat(extension).isEqualTo(".jpg");
    }

    @Test
    @DisplayName("getExtension 메소드 테스트 - null 파일명")
    void getExtension_nullFilename() {
        // given
        String filename = null;

        // when
        String extension = ReflectionTestUtils.invokeMethod(fileStorageService, "getExtension", filename);

        // then
        assertThat(extension).isEqualTo("");
    }

    @Test
    @DisplayName("getExtension 메소드 테스트 - 확장자 없는 파일명")
    void getExtension_filenameWithoutExtension() {
        // given
        String filename = "testfile";

        // when
        String extension = ReflectionTestUtils.invokeMethod(fileStorageService, "getExtension", filename);

        // then
        assertThat(extension).isEqualTo("");
    }

    @Test
    @DisplayName("파일 삭제 성공 테스트")
    void delete_success() {
        // given
        String fileName = "test-file-to-delete.jpg";
        String contentType = "image/jpeg";
        byte[] content = "test image content".getBytes();
        MultipartFile file = new MockMultipartFile("file", fileName, contentType, content);
        String savedPath = fileStorageService.upload(file);

        // when
        fileStorageService.delete(savedPath.substring(7)); // "/files/" 제외

        // then
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotNull();
        assertThat(files).isEmpty(); // 파일이 삭제되었는지 확인
    }

    @Test
    @DisplayName("파일 삭제 실패 테스트 - 파일이 존재하지 않는 경우")
    void delete_fileNotFound() {
        // given
        String nonExistentFileName = "non-existent-file.jpg";
        File file = new File(tempDir.toString(), nonExistentFileName);

        // 파일이 존재하지 않는 것 확인
        assertThat(file.exists()).isFalse();

        // when
        fileStorageService.delete(nonExistentFileName);

        // then
        // 예외가 발생하지 않고 성공적으로 완료되어야 함
    }
}