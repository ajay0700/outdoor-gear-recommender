package com.outdoor.gear.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 管理员编辑用户：禁用、角色分配等。
 */
public record AdminUserUpdateRequest(
        @Min(0) @Max(1) Integer status,
        @Size(max = 50) String nickname,
        @Size(max = 20) String phone,
        @Size(max = 100) String email,
        List<Long> roleIds
) {}
