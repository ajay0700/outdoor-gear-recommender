package com.outdoor.gear.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

/**
 * 装备更新请求（部分字段可空，空则不更新）
 */
public record GearUpdateRequest(
        String name,
        Long categoryId,
        String brand,
        @DecimalMin("0") BigDecimal price,
        BigDecimal weight,
        String season,
        String scene,
        String comfortTemperature,
        Integer maxUsers,
        @Min(0) Integer stock,
        String coverImage,
        String imageList,
        String description,
        Integer status,
        List<Long> tagIds
) {}
