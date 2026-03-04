package com.outdoor.gear.dto;

import java.math.BigDecimal;

/**
 * 购物车项 DTO
 */
public record CartItemDto(
        Long id,
        Long gearId,
        String gearName,
        BigDecimal price,
        Integer quantity,
        Boolean selected
) {}
