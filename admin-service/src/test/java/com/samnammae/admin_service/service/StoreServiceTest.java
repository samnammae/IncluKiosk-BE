package com.samnammae.admin_service.service;

import com.samnammae.admin_service.domain.store.Store;
import com.samnammae.admin_service.domain.store.StoreRepository;
import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.dto.response.StoreResponse;
import com.samnammae.admin_service.dto.response.StoreSimpleResponse;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    private static final Logger log = LoggerFactory.getLogger(StoreServiceTest.class);
    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Captor
    private ArgumentCaptor<Store> storeCaptor;

    @Test
    @DisplayName("매장 등록 성공 테스트")
    void createStore_success() {
        // given
        Long ownerId = 1L; // 가게 소유자 ID
        StoreRequest request = createStoreRequest();
        String expectedFileUrl = "stored-file.jpg";

        given(fileStorageService.upload(any())).willReturn(expectedFileUrl);

        Store savedStore = mock(Store.class);
        given(savedStore.getId()).willReturn(1L);
        given(storeRepository.save(any())).willReturn(savedStore);

        // when
        Long storeId = storeService.createStore(ownerId, request);

        // then
        assertThat(storeId).isEqualTo(1L);

        // 파일 업로드 메소드가 3번 호출되었는지 확인
        then(fileStorageService).should(times(3)).upload(any());

        // 저장된 엔티티 내용 검증
        verify(storeRepository).save(storeCaptor.capture());
        Store capturedStore = storeCaptor.getValue();

        assertThat(capturedStore.getOwnerId()).isEqualTo(ownerId);
        assertThat(capturedStore.getName()).isEqualTo(request.getName());
        assertThat(capturedStore.getPhone()).isEqualTo(request.getPhone());
        assertThat(capturedStore.getAddress()).isEqualTo(request.getAddress());
        assertThat(capturedStore.getIntroduction()).isEqualTo(request.getIntroduction());
        assertThat(capturedStore.getMainImgUrl()).isEqualTo(expectedFileUrl);
        assertThat(capturedStore.getLogoImgUrl()).isEqualTo(expectedFileUrl);
        assertThat(capturedStore.getStartBackgroundUrl()).isEqualTo(expectedFileUrl);
        assertThat(capturedStore.getMainColor()).isEqualTo(request.getMainColor());
        assertThat(capturedStore.getSubColor()).isEqualTo(request.getSubColor());
        assertThat(capturedStore.getTextColor()).isEqualTo(request.getTextColor());
    }

    @Test
    @DisplayName("파일 업로드 중 예외 발생 시 FILE_UPLOAD_FAILED 예외 발생")
    void createStore_uploadFail() {
        // given
        Long ownerId = 1L; // 가게 소유자 ID
        StoreRequest request = createStoreRequest();

        given(fileStorageService.upload(any())).willThrow(new RuntimeException("업로드 실패"));

        // when and then
        assertThatThrownBy(() -> storeService.createStore(ownerId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.FILE_UPLOAD_FAILED);
                });

        // 저장소 메소드가 호출되지 않았는지 확인
        then(storeRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미지 파일이 없을 경우에도 매장 등록 성공")
    void createStore_withoutImages_success() {
        // given
        Long ownerId = 1L; // 가게 소유자 ID
        StoreRequest request = createStoreRequestWithoutImages();

        Store savedStore = mock(Store.class);
        given(savedStore.getId()).willReturn(1L);
        given(storeRepository.save(any())).willReturn(savedStore);

        // when
        Long storeId = storeService.createStore(ownerId, request);

        // then
        assertThat(storeId).isEqualTo(1L);

        // 파일 업로드가 호출되지 않음
        then(fileStorageService).shouldHaveNoInteractions();

        // 저장된 엔티티 내용 검증
        verify(storeRepository).save(storeCaptor.capture());
        Store capturedStore = storeCaptor.getValue();

        assertThat(capturedStore.getName()).isEqualTo(request.getName());
        assertThat(capturedStore.getMainImgUrl()).isNull();
        assertThat(capturedStore.getLogoImgUrl()).isNull();
        assertThat(capturedStore.getStartBackgroundUrl()).isNull();
    }

    @Test
    @DisplayName("매장 목록 조회 성공 테스트")
    void getStoreList_success() {
        // given
        Long userId = 1L;
        List<Store> storeList = List.of(
                Store.builder()
                        .id(1L)
                        .ownerId(userId)
                        .name("테스트매장1")
                        .phone("010-1234-5678")
                        .address("서울시 어딘가1")
                        .introduction("소개1")
                        .mainImgUrl("main1.jpg")
                        .logoImgUrl("logo1.jpg")
                        .mainColor("#002F6C")
                        .build(),
                Store.builder()
                        .id(2L)
                        .ownerId(userId)
                        .name("테스트매장2")
                        .phone("010-9876-5432")
                        .address("서울시 어딘가2")
                        .introduction("소개2")
                        .mainImgUrl("main2.jpg")
                        .logoImgUrl("logo2.jpg")
                        .mainColor("#FF5733")
                        .build()
        );

        given(storeRepository.findAllByOwnerId(userId)).willReturn(storeList);

        // when
        List<StoreSimpleResponse> result = storeService.getStoreList(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStoreId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("테스트매장1");
        assertThat(result.get(0).getMainImg()).isEqualTo("main1.jpg");

        assertThat(result.get(1).getStoreId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("테스트매장2");
        assertThat(result.get(1).getMainImg()).isEqualTo("main2.jpg");
    }

    @Test
    @DisplayName("매장 목록 조회 - 매장이 없는 경우")
    void getStoreList_emptyList() {
        // given
        Long userId = 1L;
        given(storeRepository.findAllByOwnerId(userId)).willReturn(Collections.emptyList());

        // when
        List<StoreSimpleResponse> result = storeService.getStoreList(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("매장 목록 조회 - 레포지토리 호출 확인")
    void getStoreList_verifyRepositoryCall() {
        // given
        Long userId = 1L;
        given(storeRepository.findAllByOwnerId(userId)).willReturn(Collections.emptyList());

        // when
        storeService.getStoreList(userId);

        // then
        verify(storeRepository).findAllByOwnerId(userId);
    }

    @Test
    @DisplayName("특정 매장 조회 성공 테스트")
    void getStore_success() {
        // given
        Long userId = 1L;
        Long storeId = 1L;

        Store store = Store.builder()
                .id(storeId)
                .ownerId(userId)
                .name("테스트매장")
                .phone("010-1234-5678")
                .address("서울시 어딘가")
                .introduction("매장 소개입니다.")
                .mainImgUrl("main-image.jpg")
                .logoImgUrl("logo.jpg")
                .startBackgroundUrl("background.jpg")
                .mainColor("#002F6C")
                .subColor("#0051A3")
                .textColor("#F8F9FA")
                .build();

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when
        StoreResponse response = storeService.getStore(userId, storeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStoreId()).isEqualTo(storeId.toString());
        assertThat(response.getName()).isEqualTo("테스트매장");
        assertThat(response.getPhone()).isEqualTo("010-1234-5678");
        assertThat(response.getAddress()).isEqualTo("서울시 어딘가");
        assertThat(response.getMainImg()).isEqualTo("main-image.jpg");
        assertThat(response.getStartPage().getLogoImg()).isEqualTo("logo.jpg");
        assertThat(response.getStartPage().getIntroduction()).isEqualTo("매장 소개입니다.");
        assertThat(response.getStartPage().getStartBackground()).isEqualTo("background.jpg");
        assertThat(response.getTheme().getMainColor()).isEqualTo("#002F6C");
        assertThat(response.getTheme().getSubColor()).isEqualTo("#0051A3");
        assertThat(response.getTheme().getTextColor()).isEqualTo("#F8F9FA");
    }

    @Test
    @DisplayName("매장이 존재하지 않는 경우 예외 발생")
    void getStore_storeNotFound() {
        // given
        Long userId = 1L;
        Long storeId = 999L;

        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStore(userId, storeId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("다른 사용자의 매장에 접근할 경우 예외 발생")
    void getStore_forbiddenAccess() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        Long differentOwnerId = 2L; // 다른 소유자 ID

        Store store = Store.builder()
                .id(storeId)
                .ownerId(differentOwnerId) // 다른 소유자 ID 설정
                .name("테스트매장")
                .phone("010-1234-5678")
                .build();

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.getStore(userId, storeId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
                });
    }

    // 테스트에 사용할 요청 객체 생성 메소드
    private StoreRequest createStoreRequest() {
        StoreRequest request = new StoreRequest();
        request.setName("테스트매장");
        request.setPhone("010-1234-5678");
        request.setAddress("서울시 어딘가");
        request.setIntroduction("소개입니다.");
        request.setMainColor("#002F6C");
        request.setSubColor("#0051A3");
        request.setTextColor("#F8F9FA");
        request.setMainImg(new MockMultipartFile("mainImg", "main.jpg", "image/jpeg", "main".getBytes()));
        request.setLogoImg(new MockMultipartFile("logoImg", "logo.jpg", "image/jpeg", "logo".getBytes()));
        request.setStartBackground(new MockMultipartFile("startBackground", "bg.jpg", "image/jpeg", "bg".getBytes()));
        return request;
    }

    // 이미지 없는 요청 객체 생성 메소드
    private StoreRequest createStoreRequestWithoutImages() {
        StoreRequest request = new StoreRequest();
        request.setName("테스트매장");
        request.setPhone("010-1234-5678");
        request.setAddress("서울시 어딘가");
        request.setIntroduction("소개입니다.");
        request.setMainColor("#002F6C");
        request.setSubColor("#0051A3");
        request.setTextColor("#F8F9FA");
        return request;
    }
}

