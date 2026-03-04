package com.outdoor.gear.repository;

import com.outdoor.gear.entity.GearItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GearItemRepository extends JpaRepository<GearItem, Long>, JpaSpecificationExecutor<GearItem> {

    List<GearItem> findByCategoryIdAndStatusAndIsDeleted(Long categoryId, Integer status, Boolean isDeleted);
}
