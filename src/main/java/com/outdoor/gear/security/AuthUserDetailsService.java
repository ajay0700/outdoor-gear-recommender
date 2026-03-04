package com.outdoor.gear.security;

import com.outdoor.gear.entity.SysRole;
import com.outdoor.gear.entity.SysUser;
import com.outdoor.gear.repository.SysRoleRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.SysUserRoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;

    public AuthUserDetailsService(SysUserRepository userRepository,
                                  SysUserRoleRepository userRoleRepository,
                                  SysRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new UsernameNotFoundException("用户已注销: " + username);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已禁用: " + username);
        }
        List<SimpleGrantedAuthority> authorities = userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> new SimpleGrantedAuthority(opt.get().getCode()))
                .collect(Collectors.toList());
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(user.getStatus() != null && user.getStatus() == 0)
                .build();
    }
}
