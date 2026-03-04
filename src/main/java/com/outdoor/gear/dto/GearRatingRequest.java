package com.outdoor.gear.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 装备评分/评论请求
 */
public record GearRatingRequest(
        @NotNull @Min(1) @Max(5) Integer score,
        String comment
) {}
