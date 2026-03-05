package com.outdoor.gear.service;

import com.outdoor.gear.repository.GearTagRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 从用户自由文本中提取户外相关关键词，用于计划约束与推荐匹配。
 */
@Service
public class KeywordExtractorService {

    private static final List<String> FALLBACK_KEYWORDS = List.of(
            "徒步", "露营", "登山", "自驾露营", "野营", "溯溪", "攀岩", "滑雪", "钓鱼",
            "春", "夏", "秋", "冬", "四季", "夏季", "冬季",
            "轻量化", "防水", "防风", "保暖", "透气", "速干", "便携",
            "帐篷", "睡袋", "睡垫", "背包", "冲锋衣", "登山鞋",
            "单人", "双人", "多人", "家庭",
            "入门级", "性价比", "高端", "专业级"
    );

    private final GearTagRepository tagRepository;

    public KeywordExtractorService(GearTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 从文本中提取关键词，返回逗号分隔字符串
     */
    public String extractKeywords(String text) {
        if (text == null || text.isBlank()) return "";
        Set<String> keywords = new HashSet<>();
        List<String> dict = buildKeywordDict();
        for (String kw : dict) {
            if (text.contains(kw)) {
                keywords.add(kw);
            }
        }
        return keywords.stream().sorted().collect(Collectors.joining(","));
    }

    /**
     * 提取为列表，便于后续匹配
     */
    public List<String> extractAsList(String text) {
        String s = extractKeywords(text);
        if (s.isEmpty()) return List.of();
        return List.of(s.split(","));
    }

    /**
     * 从关键词中推断季节（若未显式设置）
     */
    public String inferSeason(String keywords) {
        if (keywords == null) return null;
        if (keywords.contains("春")) return "春";
        if (keywords.contains("夏") || keywords.contains("夏季")) return "夏";
        if (keywords.contains("秋")) return "秋";
        if (keywords.contains("冬") || keywords.contains("冬季")) return "冬";
        if (keywords.contains("四季")) return "四季";
        return null;
    }

    /**
     * 从关键词中推断活动类型（若未显式设置）
     */
    public String inferActivityType(String keywords) {
        if (keywords == null) return null;
        for (String a : List.of("徒步", "露营", "登山", "自驾露营", "野营", "溯溪", "攀岩", "滑雪", "钓鱼")) {
            if (keywords.contains(a)) return a;
        }
        return null;
    }

    private List<String> buildKeywordDict() {
        List<String> dict = new ArrayList<>(FALLBACK_KEYWORDS);
        try {
            tagRepository.findAll().stream()
                    .map(t -> t.getName())
                    .filter(n -> n != null && n.length() >= 2)
                    .distinct()
                    .forEach(dict::add);
        } catch (Exception ignored) {}
        return dict.stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .distinct()
                .toList();
    }
}
