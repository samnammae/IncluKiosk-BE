package com.samnammae.order_service.service;

import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuServiceFeignClient menuServiceFeignClient;

    /**
     * 주문을 생성합니다.
     * 1. 각 메뉴 정보를 Menu Service에서 조회
     * 2. 선택된 옵션 유효성 검증
     * 3. 가격 정합성 검증
     * 4. 주문 생성 및 저장
     */
    @Transactional
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto requestDto) {
        try {
            List<OrderItem> orderItems = new ArrayList<>();

            // 각 주문 아이템에 대해 메뉴 정보 조회 및 검증
            for (OrderCreateRequestDto.OrderItemDto itemDto : requestDto.getItems()) {
                // Menu Service에서 메뉴 상세 정보 조회
                MenuDetailResponseDto menuInfo = getMenuInfo(requestDto.getStoreId(), itemDto.getMenuId());

                // 주문 아이템 생성 (옵션 검증 및 가격 계산 포함)
                OrderItem orderItem = buildOrderItem(itemDto, menuInfo);
                orderItems.add(orderItem);
            }

            // 주문 총액 검증 (클라이언트 계산 vs 서버 계산)
            validateOrderTotalAmount(orderItems, requestDto.getTotalAmount());

            // 주문 엔티티 생성
            Order order = Order.builder()
                    .orderNumber(generateOrderNumber())
                    .storeId(requestDto.getStoreId())
                    .storeName(requestDto.getStoreName())
                    .orderType(requestDto.getOrderType())
                    .paymentMethod(requestDto.getPaymentMethod())
                    .status("READY")
                    .items(orderItems)
                    .totalAmount(orderItems.stream().mapToInt(OrderItem::getItemTotalPrice).sum())
                    .totalItems(requestDto.getTotalItems())
                    .build();

            Order savedOrder = orderRepository.save(order);
            log.info("주문이 성공적으로 생성되었습니다. 주문번호: {}", savedOrder.getOrderNumber());

            return new OrderCreateResponseDto(savedOrder.getId(), savedOrder.getOrderNumber());
        } catch (CustomException e) {
            log.error("주문 생성 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 주문 상세 정보를 조회합니다.
     */
    public OrderDetailResponseDto getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        return mapToOrderDetailResponseDto(order);
    }

    /**
     * 주문을 취소합니다.
     */
    @Transactional
    public OrderCancelResponseDto cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Order canceledOrder = order.cancel();
        orderRepository.save(canceledOrder);

        log.info("주문이 취소되었습니다. 주문ID: {}", orderId);
        return new OrderCancelResponseDto(order.getId(), order.getTotalAmount());
    }

    // ==================== Private Helper Methods ====================

    /**
     * Menu Service를 통해 메뉴 상세 정보를 조회합니다.
     */
    private MenuDetailResponseDto getMenuInfo(Long storeId, Long menuId) {
        try {
            return menuServiceFeignClient.getMenuDetails(storeId, menuId).getData();
        } catch (Exception e) {
            log.error("메뉴 정보 조회 실패. storeId: {}, menuId: {}", storeId, menuId, e);
            throw new CustomException(ErrorCode.MENU_NOT_FOUND);
        }
    }

    /**
     * 주문 총액을 검증합니다. (클라이언트 vs 서버 계산)
     */
    private void validateOrderTotalAmount(List<OrderItem> orderItems, int clientTotalAmount) {
        int serverCalculatedTotal = orderItems.stream().mapToInt(OrderItem::getItemTotalPrice).sum();
        if (serverCalculatedTotal != clientTotalAmount) {
            log.warn("주문 총액 불일치 - 서버: {}, 클라이언트: {}", serverCalculatedTotal, clientTotalAmount);
            throw new CustomException(ErrorCode.ORDER_TOTAL_AMOUNT_MISMATCH);
        }
    }

    /**
     * 주문 아이템을 생성합니다.
     * 1. 선택된 옵션들의 유효성 검증
     * 2. 옵션 가격 계산
     * 3. 최종 아이템 가격 검증
     */
    private OrderItem buildOrderItem(OrderCreateRequestDto.OrderItemDto itemDto, MenuDetailResponseDto menuInfo) {
        // 옵션 카테고리별 구조에서 옵션 ID로 빠르게 조회할 수 있는 맵 생성
        Map<Long, MenuDetailResponseDto.OptionDto> optionMap = menuInfo.getOptionCategories().stream()
                .flatMap(category -> category.getOptions().stream())
                .collect(Collectors.toMap(MenuDetailResponseDto.OptionDto::getOptionId, Function.identity()));

        List<SelectedOption> selectedOptions = new ArrayList<>();
        int calculatedOptionPrice = 0;

        // 클라이언트가 선택한 옵션들을 검증하고 SelectedOption 객체로 변환
        for (List<Long> optionIds : itemDto.getSelectedOptions().values()) {
            for (Long optionId : optionIds) {
                MenuDetailResponseDto.OptionDto optionInfo = optionMap.get(optionId);
                if (optionInfo == null) {
                    log.error("유효하지 않은 옵션 ID. menuId: {}, optionId: {}", itemDto.getMenuId(), optionId);
                    throw new CustomException(ErrorCode.INVALID_OPTION_ID);
                }

                // 해당 옵션의 카테고리 이름 조회
                String categoryName = findCategoryNameByOptionId(menuInfo.getOptionCategories(), optionId);

                selectedOptions.add(SelectedOption.builder()
                        .optionCategoryName(categoryName)
                        .optionName(optionInfo.getOptionName())
                        .optionPrice(optionInfo.getPrice())
                        .build());

                calculatedOptionPrice += optionInfo.getPrice();
            }
        }

        // 최종 아이템 가격 계산 및 검증
        int calculatedItemTotalPrice = (menuInfo.getBasePrice() + calculatedOptionPrice) * itemDto.getQuantity();
        if (calculatedItemTotalPrice != itemDto.getTotalPrice()) {
            log.warn("아이템 가격 불일치. 메뉴: {}, 계산가격: {}, 전송가격: {}",
                    menuInfo.getMenuName(), calculatedItemTotalPrice, itemDto.getTotalPrice());
            throw new CustomException(ErrorCode.ORDER_ITEM_PRICE_MISMATCH);
        }

        return OrderItem.builder()
                .menuId(itemDto.getMenuId())
                .menuName(menuInfo.getMenuName())
                .basePrice(menuInfo.getBasePrice())
                .quantity(itemDto.getQuantity())
                .selectedOptions(selectedOptions)
                .itemTotalPrice(calculatedItemTotalPrice)
                .build();
    }

    /**
     * 옵션 ID로 해당 옵션의 카테고리 이름을 찾습니다.
     */
    private String findCategoryNameByOptionId(List<MenuDetailResponseDto.OptionCategoryDto> categories, Long optionId) {
        return categories.stream()
                .filter(category -> category.getOptions().stream()
                        .anyMatch(option -> option.getOptionId().equals(optionId)))
                .findFirst()
                .map(MenuDetailResponseDto.OptionCategoryDto::getCategoryName)
                .orElse("알 수 없음");
    }

    /**
     * Order 엔티티를 OrderDetailResponseDto로 변환합니다.
     */
    private OrderDetailResponseDto mapToOrderDetailResponseDto(Order order) {
        List<OrderDetailResponseDto.OrderItemDto> itemDtos = order.getItems().stream()
                .map(orderItem -> OrderDetailResponseDto.OrderItemDto.builder()
                        .menuId(orderItem.getMenuId())
                        .menuName(orderItem.getMenuName())
                        .basePrice(orderItem.getBasePrice())
                        .quantity(orderItem.getQuantity())
                        .totalPrice(orderItem.getItemTotalPrice())
                        .optionPrice(orderItem.getSelectedOptions().stream().mapToInt(SelectedOption::getOptionPrice).sum())
                        // 선택된 옵션들을 카테고리별로 그룹핑
                        .selectedOptions(orderItem.getSelectedOptions().stream()
                                .collect(Collectors.groupingBy(
                                        SelectedOption::getOptionCategoryName,
                                        Collectors.mapping(SelectedOption::getOptionName, Collectors.toList())
                                )))
                        .build())
                .collect(Collectors.toList());

        return OrderDetailResponseDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .storeId(order.getStoreId())
                .storeName(order.getStoreName())
                .orderType(order.getOrderType())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .items(itemDtos)
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItems())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * 주문번호를 생성합니다. (날짜 + UUID)
     */
    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return datePart + "-" + uuidPart;
    }
}