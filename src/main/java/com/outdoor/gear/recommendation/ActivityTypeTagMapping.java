package com.outdoor.gear.recommendation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 活动类型 → 推荐用标签名映射，用于内容层扩展匹配（提升活动类型与装备标签的覆盖）
 */
public final class ActivityTypeTagMapping {

    private static final Map<String, List<String>> ACTIVITY_TO_TAGS = Map.ofEntries(
            Map.entry("徒步", List.of("徒步", "轻量化", "便携", "透气")),
            Map.entry("露营", List.of("露营", "防水", "防风", "防潮", "露营")),
            Map.entry("登山", List.of("登山", "徒步", "防风", "专业级", "轻量化")),
            Map.entry("自驾露营", List.of("自驾露营", "露营", "家庭", "四季", "便携")),
            Map.entry("野营", List.of("野营", "露营", "防水", "防风")),
            Map.entry("溯溪", List.of("溯溪", "防水", "速干", "防滑")),
            Map.entry("攀岩", List.of("攀岩", "登山", "耐磨", "专业级")),
            Map.entry("滑雪", List.of("滑雪", "防风", "保暖", "冬季")),
            Map.entry("钓鱼", List.of("钓鱼", "防水", "防晒", "便携")),
            Map.entry("长途徒步", List.of("长途徒步", "徒步", "轻量化", "透气", "可调节")),
            Map.entry("高海拔登山", List.of("高海拔登山", "登山", "防风", "专业级", "四季"))
    );

    /**
     * 根据活动类型返回推荐使用的标签名列表（含活动类型本身及扩展标签）
     */
    public static List<String> getTagNamesForActivityType(String activityType) {
        if (activityType == null || activityType.isBlank()) return List.of();
        String key = activityType.trim();
        List<String> mapped = ACTIVITY_TO_TAGS.get(key);
        if (mapped != null) return mapped;
        return List.of(key);
    }
}
