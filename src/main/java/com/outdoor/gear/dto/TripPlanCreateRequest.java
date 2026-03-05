package com.outdoor.gear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 出行计划创建/更新请求
 */
public record TripPlanCreateRequest(
        @NotBlank String name,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        Integer days,
        Integer peopleCount,
        BigDecimal budget,
        String season,
        String activityType,
        Integer difficultyLevel,
        String note,
        /** 需求描述（自由文本），系统自动提取关键词用于推荐 */
        String requirementText,
        Integer status
) {
    public Integer status() {
        return status != null ? status : 0;
    }
}
