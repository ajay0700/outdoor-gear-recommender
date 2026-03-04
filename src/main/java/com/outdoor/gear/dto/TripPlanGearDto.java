package com.outdoor.gear.dto;

/**
 * 计划-装备关联 DTO
 */
public record TripPlanGearDto(
        Long id,
        Long planId,
        Long gearId,
        String gearName,
        String source,
        Integer quantity
) {}
