package com.outdoor.gear.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50) String nickname,
        @Size(max = 20) String phone,
        @Email @Size(max = 100) String email,
        @Size(max = 255) String avatar
) {}
