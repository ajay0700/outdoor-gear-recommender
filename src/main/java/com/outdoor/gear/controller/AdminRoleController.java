package com.outdoor.gear.controller;

import com.outdoor.gear.entity.SysRole;
import com.outdoor.gear.repository.SysRoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final SysRoleRepository roleRepository;

    public AdminRoleController(SysRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<SysRole>> list() {
        return ResponseEntity.ok(roleRepository.findAll());
    }
}
