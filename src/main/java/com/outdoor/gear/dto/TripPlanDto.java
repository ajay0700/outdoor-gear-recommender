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
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TripPlanGearDto> gears
) {}
