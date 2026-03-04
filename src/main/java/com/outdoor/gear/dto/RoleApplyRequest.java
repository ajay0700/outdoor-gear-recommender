package com.outdoor.gear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 角色申请请求。仅支持申请「专家」角色。
 * 装备管理员属于管理层，只能由管理员在用户管理中授予，不可申请。
 */
public record RoleApplyRequest(
        @NotBlank(message = "角色编码不能为空")
        String roleCode,
        @Size(max = 500) String reason
) {}
