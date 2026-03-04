package com.outdoor.gear.service;

import com.outdoor.gear.dto.AuthResponse;
import com.outdoor.gear.dto.LoginRequest;
import com.outdoor.gear.dto.RegisterRequest;
import com.outdoor.gear.entity.SysRole;
import com.outdoor.gear.entity.SysUser;
import com.outdoor.gear.entity.SysUserRole;
import com.outdoor.gear.repository.SysRoleRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.SysUserRoleRepository;
import com.outdoor.gear.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(SysUserRepository userRepository,
                       SysUserRoleRepository userRoleRepository,
                       SysRoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.username() != null ? request.username().trim() : "";
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password()));
        String token = jwtTokenProvider.generateToken(auth);
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        SysUser user = userRepository.findByUsername(username).orElseThrow();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return new AuthResponse(token, user.getUsername(), user.getNickname(), roles);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username() != null ? request.username().trim() : "";
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        SysRole userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER 未初始化，请先运行 TestDataInitializer"));
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname() != null ? request.nickname() : request.username());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setStatus(1);
        user.setPoints(0);
        user.setLevel(1);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setIsDeleted(false);
        user = userRepository.save(user);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(userRole.getId());
        ur.setCreatedAt(now);
        userRoleRepository.save(ur);

        List<String> roles = List.of("ROLE_USER");
        String token = jwtTokenProvider.generateToken(user.getUsername(), roles);
        return new AuthResponse(token, user.getUsername(), user.getNickname(), roles);
    }
}
