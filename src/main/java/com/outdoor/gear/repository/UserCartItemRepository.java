package com.outdoor.gear.repository;

import com.outdoor.gear.entity.UserCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCartItemRepository extends JpaRepository<UserCartItem, Long> {

    List<UserCartItem> findByUserId(Long userId);

    List<UserCartItem> findByGearId(Long gearId);
}

