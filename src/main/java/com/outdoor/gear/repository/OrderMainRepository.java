package com.outdoor.gear.repository;

import com.outdoor.gear.entity.OrderMain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderMainRepository extends JpaRepository<OrderMain, Long> {

    Optional<OrderMain> findByOrderNo(String orderNo);

    List<OrderMain> findByUserIdAndStatusGreaterThanEqual(Long userId, Integer status);
}
