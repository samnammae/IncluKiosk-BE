package com.samnammae.admin_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.admin_service.config.TestConfig;
import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.service.StoreService;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@Import(TestConfig.class)  // 수동 빈 설정 클래스
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("매장 등록 성공 테스트")
    void registerStore_success() throws Exception {
        // given
        Long userId = 1L; // 가게 소유자 ID
        StoreRequest storeRequest = StoreRequest.builder()
                .name("테스트매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .introduction("테스트 소개")
                .mainColor("#002F6C")
                .subColor("#0051A3")
                .textColor("#F8F9FA")
                .build();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(storeRequest);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", jsonBytes);
        MockMultipartFile mainImg = new MockMultipartFile("mainImg", "main.jpg", "image/jpeg", "image-content".getBytes());
        MockMultipartFile logoImg = new MockMultipartFile("logoImg", "logo.jpg", "image/jpeg", "image-content".getBytes());
        MockMultipartFile background = new MockMultipartFile("startBackground", "bg.jpg", "image/jpeg", "image-content".getBytes());

        Mockito.when(storeService.createStore(eq(userId), any(StoreRequest.class))).thenReturn(1L);

        // when and then
        mockMvc.perform(multipart("/api/admin/store")
                        .file(requestPart)
                        .file(mainImg)
                        .file(logoImg)
                        .file(background)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("매장 등록 실패 테스트 - 파일 업로드 실패 예외 발생")
    void registerStore_fail_dueToFileUploadError() throws Exception {
        // given
        Long userId = 1L; // 가게 소유자 ID
        StoreRequest storeRequest = StoreRequest.builder()
                .name("테스트매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .mainColor("#002F6C")
                .subColor("#0051A3")
                .textColor("#F8F9FA")
                .build();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(storeRequest);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", jsonBytes);
        MockMultipartFile mainImg = new MockMultipartFile("mainImg", "main.jpg", "image/jpeg", "image-content".getBytes());

        Mockito.when(storeService.createStore(eq(userId), any(StoreRequest.class)))
                .thenThrow(new CustomException(ErrorCode.FILE_UPLOAD_FAILED));

        // when and then
        mockMvc.perform(multipart("/api/admin/store")
                        .file(requestPart)
                        .file(mainImg)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("X-User-Id", "1"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("파일 업로드에 실패했습니다."));
    }
}
