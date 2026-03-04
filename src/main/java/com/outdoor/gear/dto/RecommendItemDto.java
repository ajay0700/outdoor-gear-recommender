package com.outdoor.gear.dto;

import java.math.BigDecimal;

/**
 * 推荐结果项
 */
public record RecommendItemDto(
        Long gearId,
        String gearName,
        Long categoryId,
        String categoryName,
        String brand,
        BigDecimal price,
        String coverImage,
        Double score,
        String algorithmHint
) {}
