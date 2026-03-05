package com.outdoor.gear.repository;

import com.outdoor.gear.entity.GearRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GearRatingRepository extends JpaRepository<GearRating, Long> {

    Optional<GearRating> findByUserIdAndGearId(Long userId, Long gearId);

    List<GearRating> findByGearIdOrderByCreatedAtDesc(Long gearId);

    List<GearRating> findByUserId(Long userId);

    /** 按评分条数降序返回装备 ID，用于冷启动热门推荐 */
    @Query("SELECT r.gearId FROM GearRating r GROUP BY r.gearId ORDER BY COUNT(r) DESC")
    List<Long> findGearIdsOrderByRatingCountDesc(Pageable pageable);
}

