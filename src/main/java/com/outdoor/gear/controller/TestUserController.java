package com.outdoor.gear.controller;

import com.outdoor.gear.entity.SysUser;
import com.outdoor.gear.repository.SysUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test/users")
public class TestUserController {

    private final SysUserRepository userRepository;

    public TestUserController(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<SysUser> listUsers() {
        return userRepository.findAll();
    }
}

