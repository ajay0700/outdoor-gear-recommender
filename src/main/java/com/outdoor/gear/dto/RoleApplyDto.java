package com.outdoor.gear.dto;

import java.time.LocalDateTime;

public record RoleApplyDto(
        Long id,
        Long userId,
        String username,
        String nickname,
        String roleCode,
        String reason,
        Integer status,
        String adminNote,
        Long reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {}
