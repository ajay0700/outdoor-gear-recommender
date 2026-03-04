package com.outdoor.gear.controller;

import com.outdoor.gear.dto.AdminUserUpdateRequest;
import com.outdoor.gear.dto.UserProfileDto;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final SysUserRepository userRepository;

    public AdminUserController(UserService userService, SysUserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<UserProfileDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfileDto> result = userService.listUsers(pageable, username);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getById(@PathVariable Long id) {
        UserProfileDto dto = userService.getProfileById(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDto> update(@PathVariable Long id,
                                                  Authentication auth,
                                                  @Valid @RequestBody AdminUserUpdateRequest request) {
        Long adminUserId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        UserProfileDto dto = userService.adminUpdateUser(id, request, adminUserId);
        return ResponseEntity.ok(dto);
    }
}
