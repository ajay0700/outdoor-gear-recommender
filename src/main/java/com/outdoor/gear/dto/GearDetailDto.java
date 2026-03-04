package com.outdoor.gear.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 装备详情 DTO（含分类名、标签、评分统计、用户评分列表）
 */
public record GearDetailDto(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        String brand,
        BigDecimal price,
        BigDecimal weight,
        String season,
        String scene,
        String comfortTemperature,
        Integer maxUsers,
        Integer stock,
        String coverImage,
        String imageList,
        String description,
        Integer status,
        LocalDateTime createdAt,
        List<String> tagNames,
        Double avgScore,
        Integer ratingCount,
        List<GearRatingDto> ratings,
        Boolean isFavorite,
        Integer cartQuantity
) {}
