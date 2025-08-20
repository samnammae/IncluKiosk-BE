package com.samnammae.menu_service.domain.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 매장의 메뉴 목록을 조회
    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuCategory " +
            "LEFT JOIN FETCH m.optionCategories " +
            "WHERE m.storeId = :storeId")
    List<Menu> findAllByStoreIdWithDetails(@Param("storeId") Long storeId);

    // 특정 매장의 메뉴 상세 정보를 조회
    @Query("SELECT m FROM Menu m " +
            "LEFT JOIN FETCH m.menuCategory " +
            "LEFT JOIN FETCH m.optionCategories oc " +
            "LEFT JOIN FETCH oc.options " +
            "WHERE m.id = :menuId")
    Optional<Menu> findByIdWithDetails(@Param("menuId") Long menuId);
}
