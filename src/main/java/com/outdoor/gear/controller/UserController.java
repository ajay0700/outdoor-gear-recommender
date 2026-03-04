package com.outdoor.gear.controller;

import com.outdoor.gear.dto.UpdateProfileRequest;
import com.outdoor.gear.dto.UserProfileDto;
import com.outdoor.gear.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMe(Authentication auth) {
        String username = auth.getName();
        UserProfileDto dto = userService.getProfileByUsername(username);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMe(Authentication auth,
                                                   @Valid @RequestBody UpdateProfileRequest request) {
        String username = auth.getName();
        UserProfileDto dto = userService.updateProfile(username, request);
        return ResponseEntity.ok(dto);
    }
}
