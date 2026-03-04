package com.outdoor.gear.repository;

import com.outdoor.gear.entity.GearItemTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GearItemTagRepository extends JpaRepository<GearItemTag, Long> {

    List<GearItemTag> findByGearId(Long gearId);
}
