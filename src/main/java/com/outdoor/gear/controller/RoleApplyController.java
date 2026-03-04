package com.outdoor.gear.controller;

import com.outdoor.gear.dto.RoleApplyDto;
import com.outdoor.gear.dto.RoleApplyRequest;
import com.outdoor.gear.service.RoleApplyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/role-apply")
public class RoleApplyController {

    private final RoleApplyService roleApplyService;
    private final com.outdoor.gear.repository.SysUserRepository userRepository;

    public RoleApplyController(RoleApplyService roleApplyService,
                               com.outdoor.gear.repository.SysUserRepository userRepository) {
        this.roleApplyService = roleApplyService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<RoleApplyDto> apply(Authentication auth,
                                               @Valid @RequestBody RoleApplyRequest request) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        RoleApplyDto dto = roleApplyService.apply(userId, request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/my")
    public ResponseEntity<List<RoleApplyDto>> myApplies(Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        return ResponseEntity.ok(roleApplyService.myApplies(userId));
    }
}
