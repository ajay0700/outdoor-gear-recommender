package com.outdoor.gear.config;

import java.util.Arrays;
import java.util.Set;

/**
 * 根据装备属性生成真实风格的商品介绍（参考 8264、京东等平台格式）
 */
public final class GearDescriptionHelper {

    private static final Set<String> FEATURE_TAGS = Set.of(
            "轻量化", "防水", "防风", "透气", "保暖", "速干", "耐磨", "便携", "自充气",
            "可压缩", "羽绒", "抓绒", "尼龙", "涤纶", "钛合金", "碳纤维", "Gore-Tex"
    );

    /**
     * 根据装备属性生成商品介绍
     */
    public static String generate(String name, String brand, String category,
                                  String weight, String season, String scene,
                                  String comfortTemp, Integer maxUsers, String[] tags) {
        StringBuilder sb = new StringBuilder();
        sb.append("【产品简介】\n");
        sb.append(brand).append(" ").append(name).append("是一款专业").append(category)
                .append("，适用于").append(season != null ? season : "四季").append("季节的")
                .append(scene != null ? scene.replace(",", "、") : "徒步、露营").append("等户外场景。\n\n");

        sb.append("【产品参数】\n");
        if (weight != null && !weight.isEmpty()) {
            sb.append("· 净重：约").append(weight).append("g\n");
        }
        if (maxUsers != null && maxUsers > 0) {
            sb.append("· 适用人数：").append(maxUsers).append("人\n");
        }
        if (comfortTemp != null && !comfortTemp.isEmpty()) {
            sb.append("· 舒适温标：").append(comfortTemp).append("\n");
        }
        sb.append("· 适用季节：").append(season != null ? season : "四季").append("\n");
        sb.append("· 适用场景：").append(scene != null ? scene.replace(",", "、") : "徒步、露营").append("\n\n");

        sb.append("【产品特点】\n");
        if (tags != null && tags.length > 0) {
            String features = Arrays.stream(tags)
                    .filter(FEATURE_TAGS::contains)
                    .limit(6)
                    .reduce((a, b) -> a + "、" + b)
                    .orElse("品质可靠");
            sb.append("· 采用").append(pickMaterial(tags)).append("材质，").append(features).append("设计。\n");
        }
        sb.append("· 结构设计合理，防风防雨性能出色，透气性良好。\n");
        sb.append("· 轻便易收纳，适合背包徒步及自驾露营等多种出行方式。\n");
        sb.append("· 品牌").append(brand).append("专注户外装备多年，品质有保障。\n");

        return sb.toString();
    }

    private static String pickMaterial(String[] tags) {
        if (tags == null) return "优质";
        for (String t : tags) {
            if (t.contains("羽绒") || t.contains("尼龙") || t.contains("涤纶") || t.contains("钛合金")
                    || t.contains("碳纤维") || t.contains("抓绒") || t.contains("Gore-Tex")) {
                return t;
            }
        }
        return "优质";
    }
}
