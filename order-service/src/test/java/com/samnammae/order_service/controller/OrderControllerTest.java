package com.samnammae.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import com.samnammae.order_service.dto.request.OrderCreateRequestDto;
import com.samnammae.order_service.dto.response.OrderCancelResponseDto;
import com.samnammae.order_service.dto.response.OrderCreateResponseDto;
import com.samnammae.order_service.dto.response.OrderDetailResponseDto;
import com.samnammae.order_service.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() throws Exception {
        // given
        Map<Long, List<Long>> selectedOptions = new HashMap<>();
        selectedOptions.put(1L, Arrays.asList(1L));

        OrderCreateRequestDto.OrderItemDto orderItemDto = new OrderCreateRequestDto.OrderItemDto(
                1L, "아메리카노", 4000, selectedOptions, 0, 2, 8000
        );

        OrderCreateRequestDto requestDto = new OrderCreateRequestDto(
                1L, "테스트 매장", "STORE", "CARD", Arrays.asList(orderItemDto), 8000, 2
        );

        OrderCreateResponseDto responseDto = new OrderCreateResponseDto("order123", "20240101-ABCD1234");

        given(orderService.createOrder(any(OrderCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("order123"))
                .andExpect(jsonPath("$.data.orderNumber").value("20240101-ABCD1234"));
    }

    @Test
    @DisplayName("주문 생성 실패 - 메뉴 정보 조회 실패")
    void createOrder_MenuNotFound() throws Exception {
        // given
        Map<Long, List<Long>> selectedOptions = new HashMap<>();
        selectedOptions.put(1L, Arrays.asList(1L));

        OrderCreateRequestDto.OrderItemDto orderItemDto = new OrderCreateRequestDto.OrderItemDto(
                1L, "아메리카노", 4000, selectedOptions, 0, 2, 8000
        );

        OrderCreateRequestDto requestDto = new OrderCreateRequestDto(
                1L, "테스트 매장", "STORE", "CARD", Arrays.asList(orderItemDto), 8000, 2
        );

        given(orderService.createOrder(any(OrderCreateRequestDto.class)))
                .willThrow(new CustomException(ErrorCode.MENU_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 생성 실패 - 총 금액 불일치")
    void createOrder_TotalAmountMismatch() throws Exception {
        // given
        Map<Long, List<Long>> selectedOptions = new HashMap<>();
        selectedOptions.put(1L, Arrays.asList(1L));

        OrderCreateRequestDto.OrderItemDto orderItemDto = new OrderCreateRequestDto.OrderItemDto(
                1L, "아메리카노", 4000, selectedOptions, 0, 2, 8000
        );

        OrderCreateRequestDto requestDto = new OrderCreateRequestDto(
                1L, "테스트 매장", "STORE", "CARD", Arrays.asList(orderItemDto), 8000, 2
        );

        given(orderService.createOrder(any(OrderCreateRequestDto.class)))
                .willThrow(new CustomException(ErrorCode.ORDER_TOTAL_AMOUNT_MISMATCH));

        // when & then
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrder_Success() throws Exception {
        // given
        Map<String, List<String>> responseSelectedOptions = new HashMap<>();
        responseSelectedOptions.put("사이즈", Arrays.asList("일반"));

        OrderDetailResponseDto.OrderItemDto responseOrderItem = OrderDetailResponseDto.OrderItemDto.builder()
                .menuId(1L)
                .menuName("아메리카노")
                .basePrice(4000)
                .selectedOptions(responseSelectedOptions)
                .optionPrice(0)
                .quantity(2)
                .totalPrice(8000)
                .build();

        OrderDetailResponseDto responseDto = OrderDetailResponseDto.builder()
                .orderId("order123")
                .orderNumber("20240101-ABCD1234")
                .storeId(1L)
                .storeName("테스트 매장")
                .orderType("STORE")
                .paymentMethod("CARD")
                .status("READY")
                .items(Arrays.asList(responseOrderItem))
                .totalAmount(8000)
                .totalItems(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(orderService.getOrderById("order123"))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/order/order123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("order123"));
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 주문을 찾을 수 없음")
    void getOrder_NotFound() throws Exception {
        // given
        given(orderService.getOrderById(anyString()))
                .willThrow(new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/order/nonexistent"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() throws Exception {
        // given
        OrderCancelResponseDto responseDto = new OrderCancelResponseDto("order123", 8000);

        given(orderService.cancelOrder("order123"))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/order/order123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("order123"));
    }

    @Test
    @DisplayName("주문 취소 실패 - 주문을 찾을 수 없음")
    void cancelOrder_NotFound() throws Exception {
        // given
        given(orderService.cancelOrder(anyString()))
                .willThrow(new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(patch("/api/order/nonexistent"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 생성 실패 - 유효하지 않은 옵션 ID")
    void createOrder_InvalidOptionId() throws Exception {
        // given
        Map<Long, List<Long>> selectedOptions = new HashMap<>();
        selectedOptions.put(1L, Arrays.asList(1L));

        OrderCreateRequestDto.OrderItemDto orderItemDto = new OrderCreateRequestDto.OrderItemDto(
                1L, "아메리카노", 4000, selectedOptions, 0, 2, 8000
        );

        OrderCreateRequestDto requestDto = new OrderCreateRequestDto(
                1L, "테스트 매장", "STORE", "CARD", Arrays.asList(orderItemDto), 8000, 2
        );

        given(orderService.createOrder(any(OrderCreateRequestDto.class)))
                .willThrow(new CustomException(ErrorCode.INVALID_OPTION_ID));

        // when & then
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 생성 실패 - 아이템 가격 불일치")
    void createOrder_ItemPriceMismatch() throws Exception {
        // given
        Map<Long, List<Long>> selectedOptions = new HashMap<>();
        selectedOptions.put(1L, Arrays.asList(1L));

        OrderCreateRequestDto.OrderItemDto orderItemDto = new OrderCreateRequestDto.OrderItemDto(
                1L, "아메리카노", 4000, selectedOptions, 0, 2, 8000
        );

        OrderCreateRequestDto requestDto = new OrderCreateRequestDto(
                1L, "테스트 매장", "STORE", "CARD", Arrays.asList(orderItemDto), 8000, 2
        );

        given(orderService.createOrder(any(OrderCreateRequestDto.class)))
                .willThrow(new CustomException(ErrorCode.ORDER_ITEM_PRICE_MISMATCH));

        // when & then
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}