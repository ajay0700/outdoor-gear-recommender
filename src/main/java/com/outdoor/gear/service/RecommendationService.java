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
import com.outdoor.gear.repository.GearRatingRepository;
import com.outdoor.gear.repository.RecommendResultRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import com.outdoor.gear.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final String CACHE_KEY_PREFIX = "recommend:v3:";
    private static final long CACHE_TTL_MINUTES = 30;
    private static final double WEIGHT_CF = 0.3;
    private static final double WEIGHT_CONTENT = 0.4;
    private static final double WEIGHT_BASE = 0.3;
    /** 新装备探索分：创建时间 7 天内加此分，避免冷启动被压制 */
    private static final double EXPLORE_BONUS = 0.05;
    private static final int EXPLORE_DAYS = 7;
    /** 冷启动时热门装备加分上限（按评分条数排序的 Top 数） */
    private static final int POPULAR_TOP_N = 100;
    private static final double POPULAR_BONUS_MAX = 0.08;

    private final ConstraintFilter constraintFilter;
    private final CollaborativeFilter collaborativeFilter;
    private final ContentBasedFilter contentBasedFilter;
    private final TripPlanRepository planRepository;
    private final GearItemRepository itemRepository;
    private final GearCategoryRepository categoryRepository;
    private final GearItemTagRepository itemTagRepository;
    private final RecommendResultRepository resultRepository;
    private final GearRatingRepository ratingRepository;
    private final ObjectMapper objectMapper;
    private final UserPreferenceService preferenceService;

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
                                 GearRatingRepository ratingRepository,
                                 ObjectMapper objectMapper,
                                 UserPreferenceService preferenceService) {
        this.constraintFilter = constraintFilter;
        this.collaborativeFilter = collaborativeFilter;
        this.contentBasedFilter = contentBasedFilter;
        this.planRepository = planRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.itemTagRepository = itemTagRepository;
        this.resultRepository = resultRepository;
        this.ratingRepository = ratingRepository;
        this.objectMapper = objectMapper;
        this.preferenceService = preferenceService;
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
            if (userId != null && preferenceService != null
                    && constraint.budgetMax() == null && constraint.budgetMin() == null) {
                var pref = preferenceService.getByUserId(userId);
                if (pref.budgetMax() != null || pref.budgetMin() != null) {
                    constraint = new RecommendConstraint(
                            constraint.season(), pref.budgetMin(), pref.budgetMax(),
                            constraint.scene(), constraint.peopleCount(), constraint.categoryId(),
                            constraint.excludeGearIds());
                }
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
                ? collaborativeFilter.scoreCandidates(candidates, userId, plan)
                : Map.of();
        Map<Long, Double> contentScores;
        if (plan != null) {
            contentScores = contentBasedFilter.scoreByPlan(candidates, plan);
        } else if (userId != null) {
            Set<Long> userPreferredTags = getUserPreferredTagIds(userId);
            if (userPreferredTags.isEmpty() && preferenceService != null) {
                var pref = preferenceService.getByUserId(userId);
                Set<Long> prefTagIds = contentBasedFilter.buildTagIdsFromPreference(pref);
                userPreferredTags.addAll(prefTagIds);
            }
            if (!userPreferredTags.isEmpty()) {
                contentScores = contentBasedFilter.scoreByUserPreference(candidates, userPreferredTags);
            } else if (preferenceService != null) {
                var pref = preferenceService.getByUserId(userId);
                if ((pref.season() != null && !pref.season().isBlank())
                        || (pref.activityType() != null && !pref.activityType().isBlank())) {
                    contentScores = contentBasedFilter.scoreByExplicitPreference(
                            candidates, pref.season(), pref.activityType());
                } else {
                    contentScores = Map.of();
                }
            } else {
                contentScores = Map.of();
            }
        } else {
            contentScores = Map.of();
        }

        double maxContent = contentScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double maxCf = cfScores.isEmpty() ? 0 : cfScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        boolean coldStart = maxContent <= 0 && maxCf <= 0;
        Map<Long, Integer> popularRank = coldStart && ratingRepository != null ? buildPopularGearRank(POPULAR_TOP_N) : Map.of();

        Map<Long, Double> finalScores = new HashMap<>();
        LocalDateTime exploreThreshold = LocalDateTime.now().minus(EXPLORE_DAYS, ChronoUnit.DAYS);
        for (GearItem g : candidates) {
            double cf = normalize(cfScores.getOrDefault(g.getId(), 0.0), cfScores.values());
            double content = contentScores.getOrDefault(g.getId(), 0.0);
            double base = (constraint.season() != null || constraint.scene() != null || constraint.budgetMax() != null) ? 1.0 : 0.5;
            double explore = (g.getCreatedAt() != null && !g.getCreatedAt().isBefore(exploreThreshold)) ? EXPLORE_BONUS : 0;
            double popularBonus = 0;
            if (coldStart && popularRank.containsKey(g.getId())) {
                int rank = popularRank.get(g.getId());
                popularBonus = POPULAR_BONUS_MAX * (1.0 - (double) rank / POPULAR_TOP_N);
            }
            double score = WEIGHT_CF * cf + WEIGHT_CONTENT * content + WEIGHT_BASE * base + explore + popularBonus;
            finalScores.put(g.getId(), score);
        }

        // 按分类多样性选取：轮流从各类取最优，避免单一分类（如帐篷）垄断推荐
        int topN = request.topNValue();
        Map<Long, List<GearItem>> byCategory = candidates.stream()
                .sorted((a, b) -> Double.compare(finalScores.getOrDefault(b.getId(), 0.0), finalScores.getOrDefault(a.getId(), 0.0)))
                .collect(Collectors.groupingBy(GearItem::getCategoryId));
        List<GearItem> diverse = new ArrayList<>();
        int round = 0;
        while (diverse.size() < topN) {
            int added = 0;
            for (List<GearItem> list : byCategory.values()) {
                if (round < list.size() && diverse.size() < topN) {
                    diverse.add(list.get(round));
                    added++;
                }
            }
            if (added == 0) break;
            round++;
        }
        List<RecommendItemDto> result = new ArrayList<>(diverse.stream()
                .limit(topN)
                .map(g -> toDto(g, finalScores.get(g.getId())))
                .toList());

        if (result.size() < topN && ratingRepository != null) {
            fillWithPopularGear(result, topN, constraint.excludeGearIds());
        }

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

    /** 冷启动：按评分条数排序的装备 ID → 排名(0~topN-1) */
    private Map<Long, Integer> buildPopularGearRank(int topN) {
        try {
            List<Long> ids = ratingRepository.findGearIdsOrderByRatingCountDesc(PageRequest.of(0, topN));
            Map<Long, Integer> rank = new HashMap<>();
            for (int i = 0; i < ids.size(); i++) rank.put(ids.get(i), i);
            return rank;
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    /** 推荐条数不足时用热门装备补足（上架且未删除，排除已推荐与约束排除列表） */
    private void fillWithPopularGear(List<RecommendItemDto> result, int topN, List<Long> excludeGearIds) {
        if (result.size() >= topN) return;
        Set<Long> already = result.stream().map(RecommendItemDto::gearId).collect(Collectors.toSet());
        if (excludeGearIds != null) already.addAll(excludeGearIds);
        try {
            List<Long> popularIds = ratingRepository.findGearIdsOrderByRatingCountDesc(PageRequest.of(0, topN * 3));
            List<GearItem> toAdd = itemRepository.findAllById(popularIds).stream()
                    .filter(g -> g.getStatus() != null && g.getStatus() == 1
                            && Boolean.FALSE.equals(g.getIsDeleted())
                            && !already.contains(g.getId()))
                    .limit(topN - result.size())
                    .toList();
            for (GearItem g : toAdd) {
                result.add(toDto(g, POPULAR_BONUS_MAX));
                if (result.size() >= topN) break;
            }
        } catch (Exception ignored) {}
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
