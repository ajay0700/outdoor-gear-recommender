package com.outdoor.gear.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出行计划 DTO
 */
public record TripPlanDto(
        Long id,
        Long userId,
        String name,
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
        /** 需求描述（用户输入的自由文本） */
        String requirementText,
        /** 系统提取的关键词，逗号分隔 */
        String extractedKeywords,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TripPlanGearDto> gears
) {}
