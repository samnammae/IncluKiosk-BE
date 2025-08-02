package com.samnammae.menu_service.domain.menucategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByStoreIdOrderByDisplayOrderAsc(Long storeId);
}
