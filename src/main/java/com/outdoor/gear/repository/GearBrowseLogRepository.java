package com.outdoor.gear.repository;

import com.outdoor.gear.entity.GearBrowseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GearBrowseLogRepository extends JpaRepository<GearBrowseLog, Long> {

    List<GearBrowseLog> findByUserId(Long userId);

    List<GearBrowseLog> findByGearId(Long gearId);
}

