package com.outdoor.gear.controller;

import com.outdoor.gear.dto.RecommendItemDto;
import com.outdoor.gear.dto.RecommendRequest;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import com.outdoor.gear.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 5.4 推荐接口
 */
@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final SysUserRepository userRepository;
    private final TripPlanRepository planRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    SysUserRepository userRepository,
                                    TripPlanRepository planRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    /**
     * 按计划 ID 获取推荐（需登录且为计划所属用户）
     */
    @GetMapping("/by-plan")
    public ResponseEntity<List<RecommendItemDto>> byPlan(
            @RequestParam Long planId,
            @RequestParam(defaultValue = "20") Integer topN,
            Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("请先登录")).getId();
        var plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        RecommendRequest req = new RecommendRequest(planId, userId, null, topN);
        return ResponseEntity.ok(recommendationService.recommend(req, userId));
    }

    /**
     * 按用户 + 约束获取推荐（可匿名，仅约束过滤；登录后含协同过滤与内容推荐）
     */
    @PostMapping
    public ResponseEntity<List<RecommendItemDto>> recommend(
            @RequestBody RecommendRequest request,
            Authentication auth) {
        Long userId = auth != null && auth.isAuthenticated()
                ? userRepository.findByUsername(auth.getName()).map(u -> u.getId()).orElse(null)
                : null;
        Long effectiveUserId = request.userId() != null ? request.userId() : userId;
        return ResponseEntity.ok(recommendationService.recommend(request, effectiveUserId));
    }
}
