package com.outdoor.gear.repository;

import com.outdoor.gear.entity.GearCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GearCategoryRepository extends JpaRepository<GearCategory, Long> {

    List<GearCategory> findByParentIdOrderBySortOrderAsc(Long parentId);

    Optional<GearCategory> findByNameAndParentId(String name, Long parentId);
}
