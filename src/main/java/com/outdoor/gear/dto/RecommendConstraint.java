package com.outdoor.gear.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐约束（从出行计划或用户输入提取）
 */
public record RecommendConstraint(
        String season,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        String scene,
        Integer peopleCount,
        Long categoryId,
        List<Long> excludeGearIds
) {
    /**
     * 预算允许上浮比例，如 1.2 表示可推荐价格不超过预算 120% 的装备
     */
    public static final double BUDGET_BUFFER = 1.2;
}
