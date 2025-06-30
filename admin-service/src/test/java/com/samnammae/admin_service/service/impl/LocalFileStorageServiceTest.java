package com.samnammae.admin_service.service.impl;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LocalFileStorageServiceTest {

    // JUnit 5에서 제공하는 임시 디렉토리 기능. 각 테스트 실행 후 자동으로 정리됩니다.
    @TempDir
    Path tempDir;
    private LocalFileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService();
        // ReflectionTestUtils를 사용하여 private 필드인 uploadDir에 테스트용 임시 디렉토리 경로를 주입합니다.
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    // --- Upload Tests ---

    @Nested
    @DisplayName("파일 업로드 (upload)")
    class UploadTests {
        @Test
        @DisplayName("성공: 정상적인 파일을 업로드하면 UUID로 생성된 순수 파일명을 반환한다")
        void upload_success() throws IOException {
            // given: 테스트용 파일 준비
            String originalFileName = "test-image.jpg";
            String contentType = "image/jpeg";
            byte[] content = "test image content".getBytes();
            MultipartFile file = new MockMultipartFile("file", originalFileName, contentType, content);

            // when: 파일 업로드 실행
            String savedFileName = fileStorageService.upload(file);

            // then: 결과 검증
            assertThat(savedFileName).isNotNull();
            assertThat(savedFileName).doesNotStartWith("/"); // 반환값이 URL 경로가 아닌 순수 파일명인지 확인
            assertThat(savedFileName).endsWith(".jpg");      // 확장자가 올바르게 유지되었는지 확인

            // 파일이 실제로 임시 디렉토리에 지정된 이름으로 저장되었는지 확인
            File savedFile = new File(tempDir.toString(), savedFileName);
            assertThat(savedFile).exists();
            // 저장된 파일의 내용이 원본과 일치하는지 확인
            assertThat(Files.readAllBytes(savedFile.toPath())).isEqualTo(content);
        }

        @Test
        @DisplayName("성공: 파일이 null이거나 비어있으면 null을 반환하고 아무 파일도 생성하지 않는다")
        void upload_nullOrEmptyFile() {
            // when
            String resultForNullFile = fileStorageService.upload(null);
            String resultForEmptyFile = fileStorageService.upload(new MockMultipartFile("empty", new byte[0]));

            // then
            assertThat(resultForNullFile).isNull();
            assertThat(resultForEmptyFile).isNull();
            // 임시 디렉토리가 비어있는지 확인
            assertThat(tempDir.toFile().listFiles()).isEmpty();
        }

        @Test
        @DisplayName("실패: 파일 저장 중 IOException 발생 시 CustomException으로 전환된다")
        void upload_fail_dueToIOException() throws IOException {
            // given: I/O 예외를 발생시키는 Mock MultipartFile 준비
            MultipartFile mockFile = Mockito.mock(MultipartFile.class);
            Mockito.when(mockFile.getOriginalFilename()).thenReturn("io-error.png");
            // transferTo 호출 시 IOException을 던지도록 설정
            Mockito.doThrow(new IOException("Disk is full")).when(mockFile).transferTo(Mockito.any(File.class));

            // when & then: 예외 발생 및 타입, 에러 코드 검증
            assertThatThrownBy(() -> fileStorageService.upload(mockFile))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> {
                        CustomException ce = (CustomException) e;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.FILE_UPLOAD_FAILED);
                    });
        }
    }


    // --- Delete Tests ---

    @Nested
    @DisplayName("파일 삭제 (delete)")
    class DeleteTests {
        @Test
        @DisplayName("성공: 존재하는 파일을 정확히 삭제한다")
        void delete_success() throws IOException {
            // given: 삭제할 파일을 미리 생성
            String fileName = UUID.randomUUID() + ".txt";
            File fileToDelete = new File(tempDir.toString(), fileName);
            Files.writeString(fileToDelete.toPath(), "delete me");
            assertThat(fileToDelete).exists(); // 파일이 정상적으로 생성되었는지 확인

            // when: 파일 삭제 실행
            fileStorageService.delete(fileName);

            // then: 파일이 삭제되었는지 확인
            assertThat(fileToDelete).doesNotExist();
        }

        @Test
        @DisplayName("성공: 존재하지 않는 파일 삭제 시도 시 예외 없이 조용히 넘어간다")
        void delete_nonExistentFile() {
            // given
            String nonExistentFileName = "i-do-not-exist.jpg";

            // when & then: 예외가 발생하지 않음을 확인
            assertDoesNotThrow(() -> fileStorageService.delete(nonExistentFileName));
        }

        @Test
        @DisplayName("성공: 파일 이름이 null이거나 빈 문자열일 경우 예외 없이 조용히 넘어간다")
        void delete_nullOrEmptyFileName() {
            // when & then
            assertDoesNotThrow(() -> fileStorageService.delete(null));
            assertDoesNotThrow(() -> fileStorageService.delete(""));
            assertDoesNotThrow(() -> fileStorageService.delete("   "));
        }

        @Test
        @DisplayName("실패: 삭제 대상이 비어있지 않은 디렉토리일 경우 CustomException을 반환한다 (범용)")
        void delete_fail_whenTargetIsNonEmptyDirectory() throws IOException {
            // given: `file.delete()`가 false를 반환하는 OS 독립적인 시나리오를 구성
            // 1. 삭제할 이름으로 디렉토리를 생성
            String dirName = "non-empty-dir";
            File directory = new File(tempDir.toString(), dirName);
            directory.mkdir();

            // 2. 그 디렉토리 안에 임의의 파일을 생성하여 비어있지 않도록 만듦
            File fileInDir = new File(directory, "file.txt");
            fileInDir.createNewFile();

            assertThat(directory).exists().isDirectory(); // 디렉토리 생성 확인

            // when & then: 예외 발생 및 타입, 에러 코드 검증
            // 비어있지 않은 디렉토리에 delete()를 호출하면 false가 반환됨
            assertThatThrownBy(() -> fileStorageService.delete(dirName))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    // --- getExtension Private Method Tests (참고용) ---
    @Nested
    @DisplayName("확장자 추출 (getExtension) - private method")
    class GetExtensionTests {
        @Test
        @DisplayName("정상 파일명에서 확장자를 추출한다")
        void getExtension_normalFilename() {
            String extension = ReflectionTestUtils.invokeMethod(fileStorageService, "getExtension", "test.jpg");
            assertThat(extension).isEqualTo(".jpg");
        }

        @Test
        @DisplayName("파일명이 null일 경우 빈 문자열을 반환한다")
        void getExtension_nullFilename() {
            // null 인자를 전달할 때는 new Object[]로 감싸야 합니다.
            String extension = ReflectionTestUtils.invokeMethod(fileStorageService, "getExtension", new Object[]{null});
            assertThat(extension).isEqualTo("");
        }
    }
}