package com.outdoor.gear.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * 装备创建请求（管理员/装备管理员/专家）
 */
public record GearCreateRequest(
        @NotBlank String name,
        @NotNull Long categoryId,
        String brand,
        @NotNull @DecimalMin("0") BigDecimal price,
        BigDecimal weight,
        String season,
        String scene,
        String comfortTemperature,
        Integer maxUsers,
        @NotNull @Min(0) Integer stock,
        String coverImage,
        String imageList,
        String description,
        Integer status,
        List<Long> tagIds
) {
    public Integer status() {
        return status != null ? status : 1;
    }
}
