package com.outdoor.gear.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoleApplyReviewRequest(
        @NotNull(message = "审核结果不能为空") Integer status,
        @Size(max = 255) String adminNote
) {}