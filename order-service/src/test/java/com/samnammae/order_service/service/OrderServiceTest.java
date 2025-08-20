package com.samnammae.order_service.service;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.common.response.ApiResponse;
import com.samnammae.order_service.client.MenuServiceFeignClient;
import com.samnammae.order_service.domain.order.Order;
import com.samnammae.order_service.domain.order.OrderRepository;
import com.samnammae.order_service.domain.orderitem.OrderItem;
import com.samnammae.order_service.domain.selectedoption.SelectedOption;
import com.samnammae.order_service.dto.request.OrderCreateRequestDto;
import com.samnammae.order_service.dto.response.MenuDetailResponseDto;
import com.samnammae.order_service.dto.response.OrderCancelResponseDto;
import com.samnammae.order_service.dto.response.OrderCreateResponseDto;
import com.samnammae.order_service.dto.response.OrderDetailResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuServiceFeignClient menuServiceFeignClient;

    @InjectMocks
    private OrderService orderService;

    private OrderCreateRequestDto validOrderRequest;
    private MenuDetailResponseDto menuDetailResponse;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        // 선택된 옵션 데이터 생성
        Map<Long, List<Long>> selectedOptions = new HashMap<>();
        selectedOptions.put(1L, Arrays.asList(1L));

        // 주문 요청용 아이템 (OrderCreateRequestDto.OrderItemDto)
        OrderCreateRequestDto.OrderItemDto orderItemDto = new OrderCreateRequestDto.OrderItemDto(
                1L,                    // menuId
                "아메리카노",            // menuName
                4000,                  // basePrice
                selectedOptions,       // selectedOptions
                0,                     // optionPrice
                2,                     // quantity
                8000                   // totalPrice
        );

        // 주문 요청 생성자로 생성
        validOrderRequest = new OrderCreateRequestDto(
                1L,                           // storeId
                "테스트 매장",                  // storeName
                "STORE",                      // orderType
                "CARD",                       // paymentMethod
                Arrays.asList(orderItemDto),  // items
                8000,                         // totalAmount
                2                             // totalItems
        );

        // 메뉴 상세 응답 데이터 설정
        MenuDetailResponseDto.OptionDto option = MenuDetailResponseDto.OptionDto.builder()
                .optionId(1L)
                .optionName("일반")
                .price(0)
                .build();

        MenuDetailResponseDto.OptionCategoryDto optionCategory = MenuDetailResponseDto.OptionCategoryDto.builder()
                .categoryId(1L)
                .categoryName("사이즈")
                .options(Arrays.asList(option))
                .build();

        menuDetailResponse = MenuDetailResponseDto.builder()
                .menuId(1L)
                .menuName("아메리카노")
                .basePrice(4000)
                .description("진한 아메리카노")
                .imageUrl("test.jpg")
                .soldOut(false)
                .optionCategories(Arrays.asList(optionCategory))
                .build();

        // Order용 OrderItem 생성 (별도로 생성)
        SelectedOption selectedOption = SelectedOption.builder()
                .optionCategoryName("사이즈")
                .optionName("일반")
                .optionPrice(0)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .menuId(1L)
                .menuName("아메리카노")
                .basePrice(4000)
                .quantity(2)
                .selectedOptions(Arrays.asList(selectedOption))
                .itemTotalPrice(8000)
                .build();

        // 저장된 주문 데이터 설정
        savedOrder = Order.builder()
                .id("order123")
                .orderNumber("20240101-ABCD1234")
                .storeId(1L)
                .storeName("테스트 매장")
                .orderType("STORE")
                .paymentMethod("CARD")
                .status("READY")
                .items(Arrays.asList(orderItem)) // 올바른 OrderItem 타입 사용
                .totalAmount(8000)
                .totalItems(2)
                .build();
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        given(menuServiceFeignClient.getMenuDetails(1L, 1L))
                .willReturn(ApiResponse.success(menuDetailResponse));
        given(orderRepository.save(any(Order.class)))
                .willReturn(savedOrder);

        // when
        OrderCreateResponseDto result = orderService.createOrder(validOrderRequest);

        // then
        assertThat(result.getOrderId()).isEqualTo("order123");
        assertThat(result.getOrderNumber()).isEqualTo("20240101-ABCD1234");
        verify(menuServiceFeignClient).getMenuDetails(1L, 1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("메뉴 정보 조회 실패시 예외 발생")
    void createOrder_MenuNotFound() {
        // given
        given(menuServiceFeignClient.getMenuDetails(1L, 1L))
                .willThrow(new RuntimeException("Menu service error"));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(validOrderRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MENU_NOT_FOUND);
    }

    @Test
    @DisplayName("총 금액 불일치시 예외 발생")
    void createOrder_TotalAmountMismatch() {
        // given
        OrderCreateRequestDto invalidRequest = new OrderCreateRequestDto(
                validOrderRequest.getStoreId(),
                validOrderRequest.getStoreName(),
                validOrderRequest.getOrderType(),
                validOrderRequest.getPaymentMethod(),
                validOrderRequest.getItems(),
                9999, // 잘못된 총 금액
                validOrderRequest.getTotalItems()
        );

        given(menuServiceFeignClient.getMenuDetails(1L, 1L))
                .willReturn(ApiResponse.success(menuDetailResponse));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_TOTAL_AMOUNT_MISMATCH);
    }

    @Test
    @DisplayName("유효하지 않은 옵션 ID로 예외 발생")
    void createOrder_InvalidOptionId() {
        // given
        Map<Long, List<Long>> invalidOptions = new HashMap<>();
        invalidOptions.put(1L, Arrays.asList(999L)); // 존재하지 않는 옵션 ID

        OrderCreateRequestDto.OrderItemDto invalidOrderItem = new OrderCreateRequestDto.OrderItemDto(
                1L,
                "아메리카노",
                4000,
                invalidOptions, // 유효하지 않은 옵션
                0,
                2,
                8000
        );

        OrderCreateRequestDto invalidRequest = new OrderCreateRequestDto(
                validOrderRequest.getStoreId(),
                validOrderRequest.getStoreName(),
                validOrderRequest.getOrderType(),
                validOrderRequest.getPaymentMethod(),
                Arrays.asList(invalidOrderItem),
                validOrderRequest.getTotalAmount(),
                validOrderRequest.getTotalItems()
        );

        given(menuServiceFeignClient.getMenuDetails(1L, 1L))
                .willReturn(ApiResponse.success(menuDetailResponse));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OPTION_ID);
    }

    @Test
    @DisplayName("아이템 가격 불일치시 예외 발생")
    void createOrder_ItemPriceMismatch() {
        // given
        Map<Long, List<Long>> selectedOptions = new HashMap<>();
        selectedOptions.put(1L, Arrays.asList(1L));

        OrderCreateRequestDto.OrderItemDto invalidPriceItem = new OrderCreateRequestDto.OrderItemDto(
                1L,
                "아메리카노",
                4000,
                selectedOptions,
                0,
                2,
                9999 // 잘못된 아이템 총 가격
        );

        OrderCreateRequestDto invalidRequest = new OrderCreateRequestDto(
                validOrderRequest.getStoreId(),
                validOrderRequest.getStoreName(),
                validOrderRequest.getOrderType(),
                validOrderRequest.getPaymentMethod(),
                Arrays.asList(invalidPriceItem),
                validOrderRequest.getTotalAmount(),
                validOrderRequest.getTotalItems()
        );

        given(menuServiceFeignClient.getMenuDetails(1L, 1L))
                .willReturn(ApiResponse.success(menuDetailResponse));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ITEM_PRICE_MISMATCH);
    }

    @Test
    @DisplayName("주문 조회 성공")
    void getOrderById_Success() {
        // given
        String orderId = "order123";
        given(orderRepository.findById(orderId))
                .willReturn(Optional.of(savedOrder));

        // when
        OrderDetailResponseDto result = orderService.getOrderById(orderId);

        // then
        assertThat(result.getOrderId()).isEqualTo("order123");
        assertThat(result.getOrderNumber()).isEqualTo("20240101-ABCD1234");
        assertThat(result.getStoreId()).isEqualTo(1L);
        assertThat(result.getStoreName()).isEqualTo("테스트 매장");
        assertThat(result.getStatus()).isEqualTo("READY");
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회시 예외 발생")
    void getOrderById_NotFound() {
        // given
        String orderId = "nonexistent";
        given(orderRepository.findById(orderId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() {
        // given
        String orderId = "order123";
        given(orderRepository.findById(orderId))
                .willReturn(Optional.of(savedOrder));
        given(orderRepository.save(any(Order.class)))
                .willReturn(savedOrder.cancel());

        // when
        OrderCancelResponseDto result = orderService.cancelOrder(orderId);

        // then
        assertThat(result.getOrderId()).isEqualTo("order123");
        assertThat(result.getRefundAmount()).isEqualTo(8000);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 주문 취소시 예외 발생")
    void cancelOrder_NotFound() {
        // given
        String orderId = "nonexistent";
        given(orderRepository.findById(orderId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }
}