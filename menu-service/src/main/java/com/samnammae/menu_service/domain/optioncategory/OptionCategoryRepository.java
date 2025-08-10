package com.samnammae.menu_service.domain.optioncategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OptionCategoryRepository extends JpaRepository<OptionCategory, Long> {
    @Query("SELECT oc FROM OptionCategory oc LEFT JOIN FETCH oc.options WHERE oc.storeId = :storeId")
    List<OptionCategory> findAllByStoreIdWithDetails(@Param("storeId") Long storeId);

    Optional<OptionCategory> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByStoreIdAndName(Long storeId, String name);
}