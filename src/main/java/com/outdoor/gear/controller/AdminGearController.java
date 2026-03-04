package com.outdoor.gear.controller;

import com.outdoor.gear.dto.GearCreateRequest;
import com.outdoor.gear.dto.GearDetailDto;
import com.outdoor.gear.dto.GearListItemDto;
import com.outdoor.gear.dto.GearUpdateRequest;
import com.outdoor.gear.service.GearService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 装备管理接口（管理员/装备管理员/专家）
 */
@RestController
@RequestMapping("/api/admin/gears")
@PreAuthorize("hasAnyRole('ADMIN','GEAR_ADMIN','EXPERT')")
public class AdminGearController {

    private final GearService gearService;
    private final com.outdoor.gear.repository.SysUserRepository userRepository;

    public AdminGearController(GearService gearService,
                               com.outdoor.gear.repository.SysUserRepository userRepository) {
        this.gearService = gearService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Page<GearListItemDto> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return gearService.listForAdmin(categoryId, name, brand, priceMin, priceMax,
                season, scene, tagId, status, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GearDetailDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(gearService.getDetail(id, null));
    }

    @PostMapping
    public ResponseEntity<GearDetailDto> create(
            org.springframework.security.core.Authentication auth,
            @Valid @RequestBody GearCreateRequest request) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        return ResponseEntity.ok(gearService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GearDetailDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody GearUpdateRequest request) {
        return ResponseEntity.ok(gearService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateStatus(@PathVariable Long id,
                                                             @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            throw new IllegalArgumentException("status 不能为空");
        }
        gearService.updateStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        gearService.delete(id);
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }
}
