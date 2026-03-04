package com.outdoor.gear.repository;

import com.outdoor.gear.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<SysUser> findByUsernameContainingAndIsDeletedFalse(String username, Pageable pageable);

    Page<SysUser> findByIsDeletedFalse(Pageable pageable);
}

