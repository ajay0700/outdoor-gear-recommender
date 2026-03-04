package com.outdoor.gear.controller;

import com.outdoor.gear.dto.RoleApplyDto;
import com.outdoor.gear.dto.RoleApplyReviewRequest;
import com.outdoor.gear.service.RoleApplyService;
import com.outdoor.gear.repository.SysUserRepository;
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
@RequestMapping("/api/admin/role-apply")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleApplyController {

    private final RoleApplyService roleApplyService;
    private final SysUserRepository userRepository;

    public AdminRoleApplyController(RoleApplyService roleApplyService,
                                    SysUserRepository userRepository) {
        this.roleApplyService = roleApplyService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<RoleApplyDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoleApplyDto> result = roleApplyService.listAll(pageable, status);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<RoleApplyDto> review(@PathVariable Long id,
                                                Authentication auth,
                                                @Valid @RequestBody RoleApplyReviewRequest request) {
        Long adminUserId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        RoleApplyDto dto = roleApplyService.review(id, adminUserId, request);
        return ResponseEntity.ok(dto);
    }
}
