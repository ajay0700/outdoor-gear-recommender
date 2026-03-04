package com.outdoor.gear.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息（不包含密码），用于 GET /api/user/me 及管理员查看。
 */
public record UserProfileDto(
        Long id,
        String username,
        String nickname,
        String phone,
        String email,
        String avatar,
        Integer status,
        Integer points,
        Integer level,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        List<String> roles
) {}
