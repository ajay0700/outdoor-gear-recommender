package com.outdoor.gear.repository;

import com.outdoor.gear.entity.RoleApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleApplyRepository extends JpaRepository<RoleApply, Long> {

    List<RoleApply> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<RoleApply> findByUserIdAndStatus(Long userId, Integer status);

    Optional<RoleApply> findByUserIdAndRoleCodeAndStatus(Long userId, String roleCode, Integer status);

    Page<RoleApply> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    Page<RoleApply> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
