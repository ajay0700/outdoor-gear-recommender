package com.outdoor.gear.dto;

import java.math.BigDecimal;

/**
 * 用户偏好 DTO，用于更细致的用户画像
 */
public record UserPreferenceDto(
        String season,
        String activityType,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        /** 偏好目的地，逗号分隔，如：稻城亚丁,西藏 */
        String preferredDestinations,
        /** 偏好装备分类，逗号分隔，如：帐篷,睡袋 */
        String preferredCategories,
        /** 偏好标签，逗号分隔，如：轻量化,防水 */
        String preferredTags,
        /** 难度偏好：入门/进阶/专业 */
        String difficultyPreference
) {
    public static UserPreferenceDto of(String season, String activityType, BigDecimal budgetMin, BigDecimal budgetMax,
                                      String preferredDestinations, String preferredCategories, String preferredTags,
                                      String difficultyPreference) {
        return new UserPreferenceDto(season, activityType, budgetMin, budgetMax,
                preferredDestinations, preferredCategories, preferredTags, difficultyPreference);
    }
}
