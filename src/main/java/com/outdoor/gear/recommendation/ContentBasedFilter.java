package com.outdoor.gear.recommendation;

import com.outdoor.gear.dto.RecommendConstraint;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.GearItemTag;
import com.outdoor.gear.entity.TripPlan;
import com.outdoor.gear.repository.GearItemTagRepository;
import com.outdoor.gear.repository.GearTagRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 5.3 基于内容推荐（第三层）
 * 利用装备属性（标签、分类、场景、季节）与计划/用户偏好计算相似度
 */
@Component
public class ContentBasedFilter {

    private final GearItemTagRepository itemTagRepository;
    private final GearTagRepository tagRepository;

    public ContentBasedFilter(GearItemTagRepository itemTagRepository,
                             GearTagRepository tagRepository) {
        this.itemTagRepository = itemTagRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * 根据计划属性与装备属性计算内容相似度得分（0~1）
     */
    public Map<Long, Double> scoreByPlan(List<GearItem> candidates, TripPlan plan) {
        Set<Long> planTagIds = getPlanPreferredTagIds(plan);
        String planSeason = plan.getSeason();
        String planScene = plan.getActivityType();
        Long planCategoryHint = null;

        Map<Long, Double> scores = new HashMap<>();
        for (GearItem gear : candidates) {
            double score = 0;
            int factors = 0;

            if (!planTagIds.isEmpty()) {
                Set<Long> gearTagIds = itemTagRepository.findByGearId(gear.getId()).stream()
                        .map(GearItemTag::getTagId)
                        .collect(Collectors.toSet());
                double tagOverlap = jaccardSimilarity(planTagIds, gearTagIds);
                score += tagOverlap;
                factors++;
            }
            if (planSeason != null && !planSeason.isBlank() && gear.getSeason() != null
                    && gear.getSeason().contains(planSeason.trim())) {
                score += 1.0;
                factors++;
            }
            if (planScene != null && !planScene.isBlank() && gear.getScene() != null
                    && gear.getScene().contains(planScene.trim())) {
                score += 1.0;
                factors++;
            }
            if (plan.getPeopleCount() != null && gear.getMaxUsers() != null
                    && gear.getMaxUsers() >= plan.getPeopleCount()) {
                score += 0.5;
                factors++;
            }
            if (factors > 0) {
                scores.put(gear.getId(), score / factors);
            }
        }
        return scores;
    }

    /**
     * 根据用户历史偏好标签与装备计算相似度
     */
    public Map<Long, Double> scoreByUserPreference(List<GearItem> candidates, Set<Long> userPreferredTagIds) {
        if (userPreferredTagIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, Double> scores = new HashMap<>();
        for (GearItem gear : candidates) {
            Set<Long> gearTagIds = itemTagRepository.findByGearId(gear.getId()).stream()
                    .map(GearItemTag::getTagId)
                    .collect(Collectors.toSet());
            double sim = jaccardSimilarity(userPreferredTagIds, gearTagIds);
            if (sim > 0) {
                scores.put(gear.getId(), sim);
            }
        }
        return scores;
    }

    /**
     * 从计划推断偏好标签（根据活动类型、季节等映射到标签）
     * 简化：从已有装备标签中选与 scene/season 相关的
     */
    private Set<Long> getPlanPreferredTagIds(TripPlan plan) {
        Set<Long> tagIds = new HashSet<>();
        if (plan.getActivityType() != null && !plan.getActivityType().isBlank()) {
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && t.getName().contains(plan.getActivityType().trim()))
                    .map(t -> t.getId())
                    .forEach(tagIds::add);
        }
        if (plan.getSeason() != null && !plan.getSeason().isBlank()) {
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && t.getName().contains(plan.getSeason().trim()))
                    .map(t -> t.getId())
                    .forEach(tagIds::add);
        }
        return tagIds;
    }

    /**
     * Jaccard 相似度：|A ∩ B| / |A ∪ B|
     */
    private double jaccardSimilarity(Set<Long> a, Set<Long> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        Set<Long> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<Long> union = new HashSet<>(a);
        union.addAll(b);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }
}
