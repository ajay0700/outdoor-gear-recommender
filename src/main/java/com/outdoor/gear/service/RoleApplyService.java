package com.outdoor.gear.service;

import com.outdoor.gear.dto.RoleApplyDto;
import com.outdoor.gear.dto.RoleApplyRequest;
import com.outdoor.gear.dto.RoleApplyReviewRequest;
import com.outdoor.gear.entity.RoleApply;
import com.outdoor.gear.entity.SysRole;
import com.outdoor.gear.entity.SysUser;
import com.outdoor.gear.entity.SysUserRole;
import com.outdoor.gear.repository.RoleApplyRepository;
import com.outdoor.gear.repository.SysRoleRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.SysUserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleApplyService {

    private final RoleApplyRepository applyRepository;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;

    public RoleApplyService(RoleApplyRepository applyRepository,
                            SysUserRepository userRepository,
                            SysRoleRepository roleRepository,
                            SysUserRoleRepository userRoleRepository) {
        this.applyRepository = applyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public RoleApplyDto apply(Long userId, RoleApplyRequest request) {
        String roleCode = request.roleCode() != null ? request.roleCode().trim() : "";
        if (!"ROLE_EXPERT".equals(roleCode)) {
            throw new IllegalArgumentException("仅支持申请专家角色，装备管理员由管理员授予");
        }
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (applyRepository.findByUserIdAndRoleCodeAndStatus(userId, roleCode, RoleApply.STATUS_PENDING).isPresent()) {
            throw new IllegalArgumentException("已有待审核的申请，请勿重复提交");
        }
        SysRole role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        if (userRoleRepository.findByUserId(userId).stream()
                .anyMatch(ur -> ur.getRoleId().equals(role.getId()))) {
            throw new IllegalArgumentException("您已拥有该角色");
        }
        RoleApply apply = new RoleApply();
        apply.setUserId(userId);
        apply.setRoleCode(roleCode);
        apply.setReason(request.reason());
        apply.setStatus(RoleApply.STATUS_PENDING);
        LocalDateTime now = LocalDateTime.now();
        apply.setCreatedAt(now);
        apply.setUpdatedAt(now);
        apply = applyRepository.save(apply);
        return toDto(apply, user);
    }

    public Page<RoleApplyDto> listPending(Pageable pageable) {
        return applyRepository.findByStatusOrderByCreatedAtDesc(RoleApply.STATUS_PENDING, pageable)
                .map(this::toDtoWithUser);
    }

    public Page<RoleApplyDto> listAll(Pageable pageable, Integer status) {
        Page<RoleApply> page = status != null
                ? applyRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : applyRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(this::toDtoWithUser);
    }

    @Transactional
    public RoleApplyDto review(Long applyId, Long adminUserId, RoleApplyReviewRequest request) {
        RoleApply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在"));
        if (apply.getStatus() != RoleApply.STATUS_PENDING) {
            throw new IllegalArgumentException("该申请已处理");
        }
        if (request.status() != RoleApply.STATUS_APPROVED && request.status() != RoleApply.STATUS_REJECTED) {
            throw new IllegalArgumentException("无效的审核状态");
        }
        apply.setStatus(request.status());
        apply.setAdminNote(request.adminNote());
        apply.setReviewedBy(adminUserId);
        apply.setReviewedAt(LocalDateTime.now());
        apply.setUpdatedAt(LocalDateTime.now());
        apply = applyRepository.save(apply);

        if (request.status() == RoleApply.STATUS_APPROVED) {
            SysRole role = roleRepository.findByCode(apply.getRoleCode()).orElseThrow();
            SysUserRole ur = new SysUserRole();
            ur.setUserId(apply.getUserId());
            ur.setRoleId(role.getId());
            ur.setCreatedAt(LocalDateTime.now());
            userRoleRepository.save(ur);
        }
        return toDtoWithUser(apply);
    }

    public List<RoleApplyDto> myApplies(Long userId) {
        return applyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDtoWithUser)
                .toList();
    }

    private RoleApplyDto toDto(RoleApply apply, SysUser user) {
        return new RoleApplyDto(
                apply.getId(),
                apply.getUserId(),
                user != null ? user.getUsername() : null,
                user != null ? user.getNickname() : null,
                apply.getRoleCode(),
                apply.getReason(),
                apply.getStatus(),
                apply.getAdminNote(),
                apply.getReviewedBy(),
                apply.getReviewedAt(),
                apply.getCreatedAt()
        );
    }

    private RoleApplyDto toDtoWithUser(RoleApply apply) {
        SysUser user = userRepository.findById(apply.getUserId()).orElse(null);
        return toDto(apply, user);
    }
}
