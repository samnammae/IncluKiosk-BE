package com.samnammae.admin_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.admin_service.config.TestConfig;
import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.dto.response.StoreResponse;
import com.samnammae.admin_service.dto.response.StoreSimpleResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("매장 목록 조회 성공 테스트")
    void getStoreList_success() throws Exception {
        // given
        Long userId = 1L; // 가게 소유자 ID
        StoreSimpleResponse storeResponse1 = StoreSimpleResponse.builder()
                .storeId(1L)
                .name("첫번째매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .build();
        StoreSimpleResponse storeResponse2 = StoreSimpleResponse.builder()
                .storeId(2L)
                .name("두번째매장")
                .phone("010-9876-5432")
                .address("서울시 다른 곳")
                .build();

        List<StoreSimpleResponse> storeList = List.of(storeResponse1, storeResponse2);
        Mockito.when(storeService.getStoreList(userId)).thenReturn(storeList);

        // when and then
        mockMvc.perform(get("/api/admin/store")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].storeId").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("첫번째매장"))
                .andExpect(jsonPath("$.data[1].storeId").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("두번째매장"));
    }

    @Test
    @DisplayName("매장 정보 조회 성공 테스트")
    void getStore_success() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 1L;

        StoreResponse storeResponse = StoreResponse.builder()
                .storeId("1")
                .name("테스트매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .mainImg("main-image.jpg")
                .startPage(new StoreResponse.StartPage("logo.jpg", "매장 소개입니다.", "background.jpg"))
                .theme(new StoreResponse.Theme("#002F6C", "#0051A3", "#F8F9FA"))
                .build();

        Mockito.when(storeService.getStore(userId, storeId)).thenReturn(storeResponse);

        // when & then
        mockMvc.perform(get("/api/admin/store/{storeId}", storeId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storeId").value("1"))
                .andExpect(jsonPath("$.data.name").value("테스트매장"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.address").value("서울시 어딘가"))
                .andExpect(jsonPath("$.data.mainImg").value("main-image.jpg"))
                .andExpect(jsonPath("$.data.startPage.logoImg").value("logo.jpg"))
                .andExpect(jsonPath("$.data.theme.mainColor").value("#002F6C"));
    }

    @Test
    @DisplayName("매장 정보 조회 실패 테스트 - 존재하지 않는 매장")
    void getStore_fail_storeNotFound() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 999L;

        Mockito.when(storeService.getStore(userId, storeId))
                .thenThrow(new CustomException(ErrorCode.STORE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/admin/store/{storeId}", storeId)
                        .header("X-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.STORE_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.STORE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("매장 정보 조회 실패 테스트 - 권한 없음")
    void getStore_fail_forbidden() throws Exception {
        // given
        Long userId = 1L;
        Long storeId = 2L;

        Mockito.when(storeService.getStore(userId, storeId))
                .thenThrow(new CustomException(ErrorCode.FORBIDDEN_ACCESS));

        // when & then
        mockMvc.perform(get("/api/admin/store/{storeId}", storeId)
                        .header("X-User-Id", userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS.getMessage()));
    }

    @Test
    @DisplayName("매장 정보 수정 성공 테스트")
    void updateStore_success() throws Exception {
        // given
        Long userId = 1L; // 가게 소유자 ID
        Long storeId = 1L; // 수정할 매장 ID
        StoreRequest storeRequest = StoreRequest.builder()
                .name("수정된매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .introduction("수정된 소개")
                .mainColor("#002F6C")
                .subColor("#0051A3")
                .textColor("#F8F9FA")
                .build();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(storeRequest);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", jsonBytes);
        MockMultipartFile mainImg = new MockMultipartFile("mainImg", "main.jpg", "image/jpeg", "image-content".getBytes());
        MockMultipartFile logoImg = new MockMultipartFile("logoImg", "logo.jpg", "image/jpeg", "image-content".getBytes());
        MockMultipartFile background = new MockMultipartFile("startBackground", "bg.jpg", "image/jpeg", "image-content".getBytes());

        StoreResponse updatedStoreResponse = StoreResponse.builder()
                .storeId("1")
                .name("수정된매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .mainImg("main-image.jpg")
                .startPage(new StoreResponse.StartPage("logo.jpg", "매장 소개입니다.", "background.jpg"))
                .theme(new StoreResponse.Theme("#002F6C", "#0051A3", "#F8F9FA"))
                .build();

        Mockito.when(storeService.updateStore(eq(userId), eq(storeId), any(StoreRequest.class)))
                .thenReturn(updatedStoreResponse);

        // when and then
        mockMvc.perform(multipart("/api/admin/store/{storeId}", storeId)
                        .file(requestPart)
                        .file(mainImg)
                        .file(logoImg)
                        .file(background)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("X-User-Id", userId)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storeId").value("1"))
                .andExpect(jsonPath("$.data.name").value("수정된매장"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.address").value("서울시 어딘가"))
                .andExpect(jsonPath("$.data.mainImg").value("main-image.jpg"))
                .andExpect(jsonPath("$.data.startPage.logoImg").value("logo.jpg"))
                .andExpect(jsonPath("$.data.theme.mainColor").value("#002F6C"));
    }

    @Test
    @DisplayName("매장 정보 수정 실패 테스트 - 매장 존재하지 않음")
    void updateStore_fail_storeNotFound() throws Exception {
        // given
        Long userId = 1L; // 가게 소유자 ID
        Long storeId = 999L; // 존재하지 않는 매장 ID
        StoreRequest storeRequest = StoreRequest.builder()
                .name("수정된매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .introduction("수정된 소개")
                .mainColor("#002F6C")
                .subColor("#0051A3")
                .textColor("#F8F9FA")
                .build();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(storeRequest);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", jsonBytes);
        MockMultipartFile mainImg = new MockMultipartFile("mainImg", "main.jpg", "image/jpeg", "image-content".getBytes());

        Mockito.when(storeService.updateStore(eq(userId), eq(storeId), any(StoreRequest.class)))
                .thenThrow(new CustomException(ErrorCode.STORE_NOT_FOUND));

        // when and then
        mockMvc.perform(multipart("/api/admin/store/{storeId}", storeId)
                        .file(requestPart)
                        .file(mainImg)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("X-User-Id", userId)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.STORE_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.STORE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("매장 정보 수정 실패 테스트 - 권한 없음")
    void updateStore_fail_forbidden() throws Exception {
        // given
        Long userId = 1L; // 가게 소유자 ID
        Long storeId = 2L; // 다른 사용자의 매장 ID
        StoreRequest storeRequest = StoreRequest.builder()
                .name("수정된매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .introduction("수정된 소개")
                .mainColor("#002F6C")
                .subColor("#0051A3")
                .textColor("#F8F9FA")
                .build();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(storeRequest);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", jsonBytes);
        MockMultipartFile mainImg = new MockMultipartFile("mainImg", "main.jpg", "image/jpeg", "image-content".getBytes());

        Mockito.when(storeService.updateStore(eq(userId), eq(storeId), any(StoreRequest.class)))
                .thenThrow(new CustomException(ErrorCode.FORBIDDEN_ACCESS));

        // when and then
        mockMvc.perform(multipart("/api/admin/store/{storeId}", storeId)
                        .file(requestPart)
                        .file(mainImg)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("X-User-Id", userId)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS.getMessage()));
    }

    @Test
    @DisplayName("매장 정보 수정 실패 테스트 - 파일 업로드 실패")
    void updateStore_fail_dueToFileUploadError() throws Exception {
        // given
        Long userId = 1L; // 가게 소유자 ID
        Long storeId = 1L; // 수정할 매장 ID
        StoreRequest storeRequest = StoreRequest.builder()
                .name("수정된매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .introduction("수정된 소개")
                .mainColor("#002F6C")
                .subColor("#0051A3")
                .textColor("#F8F9FA")
                .build();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(storeRequest);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", jsonBytes);
        MockMultipartFile mainImg = new MockMultipartFile("mainImg", "main.jpg", "image/jpeg", "image-content".getBytes());

        Mockito.when(storeService.updateStore(eq(userId), eq(storeId), any(StoreRequest.class)))
                .thenThrow(new CustomException(ErrorCode.FILE_UPLOAD_FAILED));

        // when and then
        mockMvc.perform(multipart("/api/admin/store/{storeId}", storeId)
                        .file(requestPart)
                        .file(mainImg)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("X-User-Id", userId)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("파일 업로드에 실패했습니다."));
    }

}
