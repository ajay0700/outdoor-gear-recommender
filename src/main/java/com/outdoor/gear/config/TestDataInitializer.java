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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(SysRoleRepository roleRepository,
                               SysUserRepository userRepository,
                               SysUserRoleRepository userRoleRepository,
                               PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 初始化几个基础角色
        SysRole adminRole = roleRepository.findByCode("ROLE_ADMIN")
                .orElseGet(() -> createRole("ROLE_ADMIN", "系统管理员"));
        SysRole userRole = roleRepository.findByCode("ROLE_USER")
                .orElseGet(() -> createRole("ROLE_USER", "注册用户"));
        roleRepository.findByCode("ROLE_GEAR_ADMIN")
                .orElseGet(() -> createRole("ROLE_GEAR_ADMIN", "装备管理员"));
        roleRepository.findByCode("ROLE_EXPERT")
                .orElseGet(() -> createRole("ROLE_EXPERT", "专家"));

        // 初始化或同步测试管理员账号（密码 BCrypt 加密；若已有明文密码则更新为 BCrypt）
        SysUser admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new SysUser();
            admin.setUsername("admin");
            admin.setNickname("平台管理员");
            admin.setStatus(1);
            admin.setPoints(0);
            admin.setLevel(1);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setIsDeleted(false);
            admin = userRepository.save(admin);

            SysUserRole userAdmin = new SysUserRole();
            userAdmin.setUserId(admin.getId());
            userAdmin.setRoleId(adminRole.getId());
            userAdmin.setCreatedAt(LocalDateTime.now());
            userRoleRepository.save(userAdmin);

            SysUserRole userBasic = new SysUserRole();
            userBasic.setUserId(admin.getId());
            userBasic.setRoleId(userRole.getId());
            userBasic.setCreatedAt(LocalDateTime.now());
            userRoleRepository.save(userBasic);
        }
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);
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

