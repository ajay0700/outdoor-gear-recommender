package com.outdoor.gear.recommendation;

import com.outdoor.gear.dto.RecommendConstraint;
import com.outdoor.gear.dto.UserPreferenceDto;
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
     * 根据显式偏好（季节、活动类型）计算内容得分，用于冷启动
     * 用户注册时填写的偏好，或用户偏好表中的数据
     */
    public Map<Long, Double> scoreByExplicitPreference(List<GearItem> candidates,
                                                        String season, String activityType) {
        Set<Long> preferredTagIds = new HashSet<>();
        addTagIdsFromActivityType(preferredTagIds, activityType);
        if (season != null && !season.isBlank()) {
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && t.getName().contains(season.trim()))
                    .map(t -> t.getId())
                    .forEach(preferredTagIds::add);
        }
        if (preferredTagIds.isEmpty()) {
            return scoreBySeasonAndSceneOnly(candidates, season, activityType);
        }
        Map<Long, Double> scores = new HashMap<>();
        for (GearItem gear : candidates) {
            double score = 0;
            int factors = 0;
            Set<Long> gearTagIds = itemTagRepository.findByGearId(gear.getId()).stream()
                    .map(GearItemTag::getTagId)
                    .collect(Collectors.toSet());
            double tagOverlap = jaccardSimilarity(preferredTagIds, gearTagIds);
            score += tagOverlap;
            factors++;
            if (season != null && !season.isBlank() && gear.getSeason() != null
                    && gear.getSeason().contains(season.trim())) {
                score += 1.0;
                factors++;
            }
            if (activityType != null && !activityType.isBlank() && gear.getScene() != null
                    && gear.getScene().contains(activityType.trim())) {
                score += 1.0;
                factors++;
            }
            if (factors > 0) {
                scores.put(gear.getId(), score / factors);
            }
        }
        return scores;
    }

    private Map<Long, Double> scoreBySeasonAndSceneOnly(List<GearItem> candidates,
                                                         String season, String activityType) {
        Map<Long, Double> scores = new HashMap<>();
        for (GearItem gear : candidates) {
            double score = 0;
            int factors = 0;
            if (season != null && !season.isBlank() && gear.getSeason() != null
                    && gear.getSeason().contains(season.trim())) {
                score += 1.0;
                factors++;
            }
            if (activityType != null && !activityType.isBlank() && gear.getScene() != null
                    && gear.getScene().contains(activityType.trim())) {
                score += 1.0;
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
     * 从用户偏好 DTO 构建偏好标签 ID 集合，用于冷启动推荐
     */
    public Set<Long> buildTagIdsFromPreference(UserPreferenceDto pref) {
        Set<Long> tagIds = new HashSet<>();
        if (pref.season() != null && !pref.season().isBlank()) {
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && t.getName().contains(pref.season().trim()))
                    .map(t -> t.getId())
                    .forEach(tagIds::add);
        }
        addTagIdsFromActivityType(tagIds, pref.activityType());
        addTagIdsFromCommaString(tagIds, pref.preferredTags());
        addTagIdsFromCommaString(tagIds, pref.preferredCategories());
        addTagIdsFromDifficultyPreference(tagIds, pref.difficultyPreference());
        return tagIds;
    }

    /**
     * 将难度偏好映射到装备标签：入门→入门级/新手，进阶→进阶，专业→专业/专业级
     */
    private void addTagIdsFromDifficultyPreference(Set<Long> tagIds, String difficultyPreference) {
        if (difficultyPreference == null || difficultyPreference.isBlank()) return;
        String d = difficultyPreference.trim();
        List<String> mapped = switch (d) {
            case "入门" -> List.of("入门级", "新手");
            case "进阶" -> List.of("进阶");
            case "专业" -> List.of("专业", "专业级");
            default -> List.of(d);
        };
        for (String name : mapped) {
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && t.getName().equals(name))
                    .map(t -> t.getId())
                    .forEach(tagIds::add);
        }
    }

    private void addTagIdsFromCommaString(Set<Long> tagIds, String s) {
        if (s == null || s.isBlank()) return;
        for (String part : s.split(",")) {
            String k = part.trim();
            if (k.isEmpty()) continue;
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && (t.getName().equals(k) || t.getName().contains(k)))
                    .map(t -> t.getId())
                    .forEach(tagIds::add);
        }
    }

    /**
     * 活动类型 → 标签 ID：先用映射表扩展（徒步→[徒步,轻量化,便携] 等），再按名称包含查库
     */
    private void addTagIdsFromActivityType(Set<Long> tagIds, String activityType) {
        if (activityType == null || activityType.isBlank()) return;
        for (String tagName : ActivityTypeTagMapping.getTagNamesForActivityType(activityType)) {
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && (t.getName().equals(tagName) || t.getName().contains(tagName)))
                    .map(t -> t.getId())
                    .forEach(tagIds::add);
        }
    }

    /**
     * 从计划推断偏好标签（根据活动类型、季节等映射到标签）
     */
    private Set<Long> getPlanPreferredTagIds(TripPlan plan) {
        Set<Long> tagIds = new HashSet<>();
        addTagIdsFromActivityType(tagIds, plan.getActivityType());
        if (plan.getSeason() != null && !plan.getSeason().isBlank()) {
            tagRepository.findAll().stream()
                    .filter(t -> t.getName() != null && t.getName().contains(plan.getSeason().trim()))
                    .map(t -> t.getId())
                    .forEach(tagIds::add);
        }
        if (plan.getExtractedKeywords() != null && !plan.getExtractedKeywords().isBlank()) {
            for (String kw : plan.getExtractedKeywords().split(",")) {
                String k = kw.trim();
                if (k.isEmpty()) continue;
                tagRepository.findAll().stream()
                        .filter(t -> t.getName() != null && (t.getName().equals(k) || t.getName().contains(k)))
                        .map(t -> t.getId())
                        .forEach(tagIds::add);
            }
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
