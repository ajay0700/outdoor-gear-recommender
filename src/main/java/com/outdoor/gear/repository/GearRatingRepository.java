package com.outdoor.gear.repository;

import com.outdoor.gear.entity.GearRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GearRatingRepository extends JpaRepository<GearRating, Long> {

    Optional<GearRating> findByUserIdAndGearId(Long userId, Long gearId);
}

