package com.outdoor.gear.dto;

/**
 * 推荐请求：计划 ID 或用户 + 约束
 */
public record RecommendRequest(
        Long planId,
        Long userId,
        RecommendConstraint constraint,
        Integer topN
) {
    public int topNValue() {
        return topN != null && topN > 0 ? Math.min(topN, 50) : 20;
    }
}
