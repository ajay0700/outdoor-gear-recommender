package com.outdoor.gear.repository;

import com.outdoor.gear.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    List<TripPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
