package com.outdoor.gear.config;

import com.outdoor.gear.entity.SysRole;
import com.outdoor.gear.entity.SysUser;
import com.outdoor.gear.entity.SysUserRole;
import com.outdoor.gear.repository.SysRoleRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.SysUserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 本地开发环境下初始化少量测试数据，验证 JPA 读写是否正常。
 */
@Component
@Profile("local")
@Order(1)
public class TestDataInitializer implements CommandLineRunner {

    private final SysRoleRepository roleRepository;
    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;

    public TestDataInitializer(SysRoleRepository roleRepository,
                               SysUserRepository userRepository,
                               SysUserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public void run(String... args) {
        // 初始化几个基础角色
        SysRole adminRole = roleRepository.findByCode("ROLE_ADMIN")
                .orElseGet(() -> createRole("ROLE_ADMIN", "系统管理员"));
        SysRole userRole = roleRepository.findByCode("ROLE_USER")
                .orElseGet(() -> createRole("ROLE_USER", "注册用户"));

        // 初始化一个测试管理员账号
        userRepository.findByUsername("admin").orElseGet(() -> {
            SysUser user = new SysUser();
            user.setUsername("admin");
            // 后续集成 Spring Security 时会改为加密密码
            user.setPassword("admin123");
            user.setNickname("平台管理员");
            user.setStatus(1);
            user.setPoints(0);
            user.setLevel(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setIsDeleted(false);
            SysUser saved = userRepository.save(user);

            SysUserRole userAdmin = new SysUserRole();
            userAdmin.setUserId(saved.getId());
            userAdmin.setRoleId(adminRole.getId());
            userAdmin.setCreatedAt(LocalDateTime.now());
            userRoleRepository.save(userAdmin);

            SysUserRole userBasic = new SysUserRole();
            userBasic.setUserId(saved.getId());
            userBasic.setRoleId(userRole.getId());
            userBasic.setCreatedAt(LocalDateTime.now());
            userRoleRepository.save(userBasic);

            return saved;
        });
    }

    private SysRole createRole(String code, String name) {
        SysRole role = new SysRole();
        role.setCode(code);
        role.setName(name);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return roleRepository.save(role);
    }
}

