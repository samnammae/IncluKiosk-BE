package com.samnammae.menu_service.domain.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 매장의 메뉴 목록을 조회
    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuCategory " +
            "LEFT JOIN FETCH m.optionCategories " +
            "WHERE m.storeId = :storeId")
    List<Menu> findAllByStoreIdWithDetails(@Param("storeId") Long storeId);
}
