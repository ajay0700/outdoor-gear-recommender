package com.outdoor.gear.repository;

import com.outdoor.gear.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    List<SysUserRole> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM SysUserRole ur WHERE ur.userId = :userId")
    void deleteByUserId(Long userId);
}

