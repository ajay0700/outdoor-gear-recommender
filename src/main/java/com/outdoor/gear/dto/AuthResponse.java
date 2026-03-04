package com.outdoor.gear.dto;

import java.util.List;

public record AuthResponse(
        String token,
        String username,
        String nickname,
        List<String> roles
) {}
