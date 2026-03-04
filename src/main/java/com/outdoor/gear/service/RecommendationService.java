package com.outdoor.gear.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.outdoor.gear.dto.RecommendConstraint;
import com.outdoor.gear.dto.RecommendItemDto;
import com.outdoor.gear.dto.RecommendRequest;
import com.outdoor.gear.entity.GearCategory;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.RecommendResult;
import com.outdoor.gear.entity.TripPlan;
import com.outdoor.gear.recommendation.CollaborativeFilter;
import com.outdoor.gear.recommendation.ConstraintFilter;
import com.outdoor.gear.recommendation.ContentBasedFilter;
import com.outdoor.gear.repository.GearCategoryRepository;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.GearItemTagRepository;
import com.outdoor.gear.repository.RecommendResultRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 5.4 统一推荐服务：约束过滤 + 协同过滤 + 基于内容，结果缓存
 */
@Service
public class RecommendationService {

    private static final String CACHE_KEY_PREFIX = "recommend:";
    private static final long CACHE_TTL_MINUTES = 30;
    private static final double WEIGHT_CF = 0.3;
    private static final double WEIGHT_CONTENT = 0.4;
    private static final double WEIGHT_BASE = 0.3;

    private final ConstraintFilter constraintFilter;
    private final CollaborativeFilter collaborativeFilter;
    private final ContentBasedFilter contentBasedFilter;
    private final TripPlanRepository planRepository;
    private final GearItemRepository itemRepository;
    private final GearCategoryRepository categoryRepository;
    private final GearItemTagRepository itemTagRepository;
    private final RecommendResultRepository resultRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    public RecommendationService(ConstraintFilter constraintFilter,
                                 CollaborativeFilter collaborativeFilter,
                                 ContentBasedFilter contentBasedFilter,
                                 TripPlanRepository planRepository,
                                 GearItemRepository itemRepository,
                                 GearCategoryRepository categoryRepository,
                                 GearItemTagRepository itemTagRepository,
                                 RecommendResultRepository resultRepository,
                                 ObjectMapper objectMapper) {
        this.constraintFilter = constraintFilter;
        this.collaborativeFilter = collaborativeFilter;
        this.contentBasedFilter = contentBasedFilter;
        this.planRepository = planRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.itemTagRepository = itemTagRepository;
        this.resultRepository = resultRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 统一推荐入口
     */
    public List<RecommendItemDto> recommend(RecommendRequest request, Long currentUserId) {
        Long planId = request.planId();
        Long userId = currentUserId;
        RecommendConstraint constraint;
        TripPlan plan = null;

        if (planId != null) {
            plan = planRepository.findById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("计划不存在"));
            userId = plan.getUserId();
            constraint = request.constraint() != null ? request.constraint() : constraintFilter.fromPlan(plan);
        } else {
            constraint = request.constraint();
            if (constraint == null) {
                constraint = new RecommendConstraint(null, null, null, null, null, null, List.of());
            }
        }

        String cacheKey = buildCacheKey(planId, userId, constraint);
        List<RecommendItemDto> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached.stream().limit(request.topNValue()).toList();
        }

        List<GearItem> candidates = planId != null && request.constraint() == null
                ? constraintFilter.filterByPlan(plan)
                : constraintFilter.filter(constraint);

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> cfScores = userId != null
                ? collaborativeFilter.scoreCandidates(candidates, userId)
                : Map.of();
        Map<Long, Double> contentScores;
        if (plan != null) {
            contentScores = contentBasedFilter.scoreByPlan(candidates, plan);
        } else if (userId != null) {
            Set<Long> userPreferredTags = getUserPreferredTagIds(userId);
            contentScores = contentBasedFilter.scoreByUserPreference(candidates, userPreferredTags);
        } else {
            contentScores = Map.of();
        }

        Map<Long, Double> finalScores = new java.util.HashMap<>();
        for (GearItem g : candidates) {
            double cf = normalize(cfScores.getOrDefault(g.getId(), 0.0), cfScores.values());
            double content = contentScores.getOrDefault(g.getId(), 0.0);
            double base = (constraint.season() != null || constraint.scene() != null || constraint.budgetMax() != null) ? 1.0 : 0.5;
            double score = WEIGHT_CF * cf + WEIGHT_CONTENT * content + WEIGHT_BASE * base;
            finalScores.put(g.getId(), score);
        }

        List<RecommendItemDto> result = candidates.stream()
                .sorted((a, b) -> Double.compare(finalScores.getOrDefault(b.getId(), 0.0), finalScores.getOrDefault(a.getId(), 0.0)))
                .limit(request.topNValue())
                .map(g -> toDto(g, finalScores.get(g.getId())))
                .toList();

        saveToCache(cacheKey, result);
        saveToResult(planId, userId, result);
        return result;
    }

    private Set<Long> getUserPreferredTagIds(Long userId) {
        Set<Long> likedGearIds = collaborativeFilter.getUserLikedGearIds(userId);
        return likedGearIds.stream()
                .flatMap(gid -> itemTagRepository.findByGearId(gid).stream())
                .map(t -> t.getTagId())
                .collect(Collectors.toSet());
    }

    private RecommendItemDto toDto(GearItem g, Double score) {
        String catName = categoryRepository.findById(g.getCategoryId()).map(GearCategory::getName).orElse("");
        return new RecommendItemDto(g.getId(), g.getName(), g.getCategoryId(), catName,
                g.getBrand(), g.getPrice(), g.getCoverImage(), score, "HYBRID");
    }

    private double normalize(double v, java.util.Collection<Double> all) {
        if (all.isEmpty() || v == 0) return 0;
        double max = all.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        return max > 0 ? v / max : 0;
    }

    private String buildCacheKey(Long planId, Long userId, RecommendConstraint c) {
        return CACHE_KEY_PREFIX + (planId != null ? "p" + planId : "u" + userId) + ":" + System.currentTimeMillis() / (CACHE_TTL_MINUTES * 60 * 1000);
    }

    private List<RecommendItemDto> getFromCache(String key) {
        if (redisTemplate == null) return null;
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<>() {});
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void saveToCache(String key, List<RecommendItemDto> result) {
        if (redisTemplate == null) return;
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {}
    }

    @Transactional
    private void saveToResult(Long planId, Long userId, List<RecommendItemDto> result) {
        try {
            RecommendResult rr = new RecommendResult();
            rr.setPlanId(planId);
            rr.setUserId(userId);
            rr.setAlgorithm("HYBRID");
            rr.setResultJson(objectMapper.writeValueAsString(result));
            rr.setCreatedAt(LocalDateTime.now());
            resultRepository.save(rr);
        } catch (Exception ignored) {}
    }
}
