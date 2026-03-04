package com.outdoor.gear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50)
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100)
        String password,
        String nickname,
        String phone,
        String email
) {}
