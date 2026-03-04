package com.outdoor.gear.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 装备列表项 DTO
 */
public record GearListItemDto(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        String brand,
        BigDecimal price,
        BigDecimal weight,
        String season,
        String scene,
        Integer stock,
        String coverImage,
        Integer status,
        LocalDateTime createdAt,
        List<String> tagNames
) {}
