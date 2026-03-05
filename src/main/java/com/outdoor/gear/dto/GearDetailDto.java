package com.outdoor.gear.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 装备详情 DTO（含分类名、标签分组、评分统计、用户评分列表、同分类推荐）
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
        /** 按类型分组的标签，如 {"特性":["轻量化","防水"], "场景":["徒步","露营"]} */
        Map<String, List<String>> tagGroups,
        Double avgScore,
        Integer ratingCount,
        List<GearRatingDto> ratings,
        Boolean isFavorite,
        Integer cartQuantity,
        /** 同分类推荐装备 */
        List<GearListItemDto> relatedGears
) {}
