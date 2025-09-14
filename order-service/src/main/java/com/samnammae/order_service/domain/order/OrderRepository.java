package com.samnammae.order_service.domain.order;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {

    // 특정 매장의 주문을 생성일 기준 내림차순으로 조회
    List<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId);
}
