package com.outdoor.gear.service;

import com.outdoor.gear.dto.AdminUserUpdateRequest;
import com.outdoor.gear.dto.UpdateProfileRequest;
import com.outdoor.gear.dto.UserProfileDto;
import com.outdoor.gear.entity.SysRole;
import com.outdoor.gear.entity.SysUser;
import com.outdoor.gear.entity.SysUserRole;
import com.outdoor.gear.repository.SysRoleRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.SysUserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;

    public UserService(SysUserRepository userRepository,
                       SysUserRoleRepository userRoleRepository,
                       SysRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    public UserProfileDto getProfileByUsername(String username) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        List<String> roles = getRoleCodes(user.getId());
        return toDto(user, roles);
    }

    @Transactional
    public UserProfileDto updateProfile(String username, UpdateProfileRequest request) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.email() != null) user.setEmail(request.email());
        if (request.avatar() != null) user.setAvatar(request.avatar());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        return toDto(user, getRoleCodes(user.getId()));
    }

    private List<String> getRoleCodes(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get().getCode())
                .collect(Collectors.toList());
    }

    public Page<UserProfileDto> listUsers(Pageable pageable, String username) {
        Page<SysUser> page = username != null && !username.isBlank()
                ? userRepository.findByUsernameContainingAndIsDeletedFalse(username, pageable)
                : userRepository.findByIsDeletedFalse(pageable);
        return page.map(user -> toDto(user, getRoleCodes(user.getId())));
    }

    public UserProfileDto getProfileById(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return toDto(user, getRoleCodes(user.getId()));
    }

    @Transactional
    public UserProfileDto adminUpdateUser(Long id, AdminUserUpdateRequest request, Long currentAdminUserId) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (request.status() != null) {
            if (request.status() == 0 && id.equals(currentAdminUserId)) {
                throw new IllegalArgumentException("不能禁用自己");
            }
            user.setStatus(request.status());
        }
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.email() != null) user.setEmail(request.email());
        user.setUpdatedAt(LocalDateTime.now());
        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            userRoleRepository.deleteByUserId(user.getId());
            LocalDateTime now = LocalDateTime.now();
            for (Long roleId : request.roleIds()) {
                if (roleRepository.existsById(roleId)) {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(user.getId());
                    ur.setRoleId(roleId);
                    ur.setCreatedAt(now);
                    userRoleRepository.save(ur);
                }
            }
        }
        user = userRepository.save(user);
        return toDto(user, getRoleCodes(user.getId()));
    }

    private UserProfileDto toDto(SysUser user, List<String> roles) {
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getStatus(),
                user.getPoints(),
                user.getLevel(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                roles
        );
    }
}
