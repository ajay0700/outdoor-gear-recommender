package com.outdoor.gear.repository;

import com.outdoor.gear.entity.TripPlanGear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPlanGearRepository extends JpaRepository<TripPlanGear, Long> {

    List<TripPlanGear> findByPlanId(Long planId);
}
