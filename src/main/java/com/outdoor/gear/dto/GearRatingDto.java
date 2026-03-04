package com.outdoor.gear.dto;

import java.time.LocalDateTime;

/**
 * 装备评分/评论 DTO
 */
public record GearRatingDto(
        Long id,
        Long userId,
        String userNickname,
        Integer score,
        String comment,
        LocalDateTime createdAt
) {}
