package com.outdoor.gear.controller;

import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户收藏接口
 */
@RestController
@RequestMapping("/api/user/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final SysUserRepository userRepository;

    public FavoriteController(FavoriteService favoriteService, SysUserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{gearId}")
    public ResponseEntity<Map<String, String>> add(@PathVariable Long gearId, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        favoriteService.add(userId, gearId);
        return ResponseEntity.ok(Map.of("message", "added"));
    }

    @DeleteMapping("/{gearId}")
    public ResponseEntity<Map<String, String>> remove(@PathVariable Long gearId, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        favoriteService.remove(userId, gearId);
        return ResponseEntity.ok(Map.of("message", "removed"));
    }
}
