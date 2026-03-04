package com.outdoor.gear.controller;

import com.outdoor.gear.dto.GearDetailDto;
import com.outdoor.gear.dto.GearListItemDto;
import com.outdoor.gear.dto.GearRatingRequest;
import com.outdoor.gear.entity.GearCategory;
import com.outdoor.gear.repository.GearCategoryRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.service.GearRatingService;
import com.outdoor.gear.service.GearService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户端装备列表、详情、对比、评分
 */
@RestController
@RequestMapping("/api/gears")
public class GearController {

    private final GearService gearService;
    private final GearRatingService ratingService;
    private final GearCategoryRepository categoryRepository;
    private final SysUserRepository userRepository;

    public GearController(GearService gearService, GearRatingService ratingService,
                          GearCategoryRepository categoryRepository, SysUserRepository userRepository) {
        this.gearService = gearService;
        this.ratingService = ratingService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/categories")
    public List<GearCategory> listCategories() {
        return categoryRepository.findByParentIdOrderBySortOrderAsc(0L);
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return gearService.listForUser(categoryId, name, brand, priceMin, priceMax,
                season, scene, tagId, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GearDetailDto> get(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null && auth.isAuthenticated()
                ? userRepository.findByUsername(auth.getName()).map(u -> u.getId()).orElse(null)
                : null;
        return ResponseEntity.ok(gearService.getDetail(id, userId));
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<Map<String, String>> rate(@PathVariable Long id,
                                                     @Valid @RequestBody GearRatingRequest request,
                                                     Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("请先登录")).getId();
        ratingService.saveOrUpdate(userId, id, request);
        return ResponseEntity.ok(Map.of("message", "saved"));
    }

    @GetMapping("/compare")
    public ResponseEntity<List<GearDetailDto>> compare(
            @RequestParam List<Long> ids,
            Authentication auth) {
        Long userId = auth != null && auth.isAuthenticated()
                ? userRepository.findByUsername(auth.getName()).map(u -> u.getId()).orElse(null)
                : null;
        return ResponseEntity.ok(gearService.getDetailsByIds(ids, userId));
    }
}
