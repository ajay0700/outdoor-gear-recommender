package com.outdoor.gear.repository;

import com.outdoor.gear.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    Optional<UserFavorite> findByUserIdAndGearId(Long userId, Long gearId);

    List<UserFavorite> findByUserId(Long userId);

    List<UserFavorite> findByGearId(Long gearId);
}

