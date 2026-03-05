package com.outdoor.gear.config;

import com.outdoor.gear.entity.GearCategory;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.GearItemTag;
import com.outdoor.gear.entity.GearTag;
import com.outdoor.gear.repository.GearCategoryRepository;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.GearItemTagRepository;
import com.outdoor.gear.repository.GearTagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 装备知识库与标签体系初始化。
 * 在 local 环境下，若知识库为空则初始化：分类、标签、示例装备及装备-标签关联。
 * 为约束过滤、基于内容推荐提供可查询维度。
 */
@Component
@Profile("local")
@Order(2) // 在 TestDataInitializer(默认1) 之后执行
public class GearKnowledgeInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GearKnowledgeInitializer.class);

    private final GearCategoryRepository categoryRepository;
    private final GearTagRepository tagRepository;
    private final GearItemRepository itemRepository;
    private final GearItemTagRepository itemTagRepository;

    @Value("${gear.init.force:false}")
    private boolean forceReinit;

    public GearKnowledgeInitializer(GearCategoryRepository categoryRepository,
                                    GearTagRepository tagRepository,
                                    GearItemRepository itemRepository,
                                    GearItemTagRepository itemTagRepository) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.itemRepository = itemRepository;
        this.itemTagRepository = itemTagRepository;
    }

    @Override
    public void run(String... args) {
        if (forceReinit) {
            log.info("gear.init.force=true，清空装备知识库并重新初始化");
            clearGearKnowledge();
        } else if (categoryRepository.count() > 0 && tagRepository.count() > 0) {
            log.info("装备知识库已存在，跳过初始化（需重新初始化时请设置 gear.init.force=true）");
            return;
        }
        log.info("开始初始化装备知识库与标签体系...");
        initCategories();
        initTags();
        initSampleItems();
        long itemCount = itemRepository.count();
        log.info("装备知识库与标签体系初始化完成，共 {} 件装备", itemCount);
    }

    /** 清空装备知识库（装备-标签、装备、标签、分类），便于重新初始化 */
    private void clearGearKnowledge() {
        itemTagRepository.deleteAll();
        itemRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        log.info("装备知识库已清空");
    }

    private void initCategories() {
        if (categoryRepository.count() > 0) return;
        String[][] cats = {
            {"帐篷", "0"}, {"睡袋", "0"}, {"睡垫", "0"}, {"背包", "0"}, {"冲锋衣", "0"},
            {"登山鞋", "0"}, {"炉具", "0"}, {"照明", "0"}, {"急救包", "0"}, {"登山杖", "0"},
            {"天幕", "0"}, {"地席", "0"}, {"水具", "0"}, {"炊具", "0"}, {"护具", "0"},
            {"速干衣", "0"}, {"抓绒衣", "0"}, {"羽绒服", "0"}, {"头灯", "0"}, {"手电", "0"}
        };
        int order = 1;
        for (String[] c : cats) {
            if (categoryRepository.findByNameAndParentId(c[0], 0L).isEmpty()) {
                GearCategory cat = new GearCategory();
                cat.setName(c[0]);
                cat.setParentId(0L);
                cat.setSortOrder(order++);
                cat.setCreatedAt(LocalDateTime.now());
                cat.setUpdatedAt(LocalDateTime.now());
                categoryRepository.save(cat);
            }
        }
    }

    private void initTags() {
        // FEATURE: 产品特性
        String[] featureTags = {
            "轻量化", "防水", "防紫外线", "透气", "保暖", "速干", "耐磨", "耐用",
            "易收纳", "折叠", "便携", "可调节", "防晒", "防寒", "四季", "防风",
            "阻燃", "防潮", "防泼水", "防撕裂", "防滑", "减震", "加厚", "透气网眼",
            "抗菌", "防臭", "自充气", "可压缩", "可拆卸", "可清洗", "可机洗",
            "应急", "多功能", "保温", "隔热", "防水拉链", "纱网", "防蚊虫",
            "通风", "双层", "单层", "三季", "高山", "低海拔", "防雨"
        };
        // SCENE: 活动场景
        String[] sceneTags = {
            "徒步", "露营", "登山", "攀岩", "骑行", "滑雪", "钓鱼", "野钓",
            "越野跑", "自驾露营", "野营", "溯溪", "沙漠穿越", "高原徒步", "雪地穿越",
            "丛林探险", "草原露营", "海边露营", "森林徒步", "城市徒步", "户外露营",
            "家庭露营", "野餐", "长途徒步", "短途徒步", "一日游", "多日徒步",
            "露营过夜", "山地骑行", "公路骑行", "高海拔登山", "攀冰", "越野滑雪",
            "雪地徒步", "水上运动", "漂流"
        };
        // SEASON: 适用季节
        String[] seasonTags = {"春", "夏", "秋", "冬", "四季", "夏季", "冬季"};
        // BUDGET: 预算区间
        String[] budgetTags = {"入门级", "经济型", "性价比", "中端", "中高端", "专业级", "高端"};
        // MATERIAL: 材质
        String[] materialTags = {
            "尼龙", "涤纶", "羽绒", "抓绒", "Gore-Tex", "碳纤维", "铝合金", "钛合金",
            "聚酯纤维", "棉", "羊毛", "羽绒填充", "化纤填充", "橡胶", "EVA", "PU",
            "硅胶", "牛津布", "涂层布", "网布"
        };
        // PERSON: 适用人数
        String[] personTags = {"单人", "双人", "多人", "家庭"};
        // SKILL: 技能等级
        String[] skillTags = {"新手", "进阶", "专业", "发烧友"};
        // WEATHER: 天气条件
        String[] weatherTags = {"晴天", "阴天", "雨天", "雪天", "大风", "高温", "低温"};

        saveTags("FEATURE", featureTags);
        saveTags("SCENE", sceneTags);
        saveTags("SEASON", seasonTags);
        saveTags("BUDGET", budgetTags);
        saveTags("MATERIAL", materialTags);
        saveTags("PERSON", personTags);
        saveTags("SKILL", skillTags);
        saveTags("WEATHER", weatherTags);
    }

    private void saveTags(String type, String[] names) {
        for (String name : names) {
            if (tagRepository.findByNameAndType(name, type).isEmpty()) {
                GearTag t = new GearTag();
                t.setName(name);
                t.setType(type);
                t.setCreatedAt(LocalDateTime.now());
                t.setUpdatedAt(LocalDateTime.now());
                tagRepository.save(t);
            }
        }
    }

    private void initSampleItems() {
        if (itemRepository.count() > 0) return;

        List<GearCategory> cats = categoryRepository.findByParentIdOrderBySortOrderAsc(0L);
        if (cats.isEmpty()) return;

        Map<String, Long> catMap = new java.util.HashMap<>();
        for (GearCategory c : cats) catMap.put(c.getName(), c.getId());

        List<GearTag> allTags = tagRepository.findAll();
        Map<String, Long> tagMap = new java.util.HashMap<>();
        for (GearTag t : allTags) tagMap.put(t.getName() + ":" + t.getType(), t.getId());

        LocalDateTime now = LocalDateTime.now();
        for (GearData.Item def : GearData.allWithVariants()) {
            Long catId = catMap.get(def.category());
            if (catId == null) catId = cats.get(0).getId();
            GearItem item = createItem(def, catId, now);
            itemRepository.save(item);
            linkTags(item.getId(), tagMap, now, def.tags());
        }
    }

    private GearItem createItem(GearData.Item def, Long catId, LocalDateTime now) {
        GearItem item = new GearItem();
        item.setName(def.name());
        item.setCategoryId(catId);
        item.setBrand(def.brand());
        item.setPrice(new BigDecimal(def.price()));
        item.setWeight(def.weight() != null ? new BigDecimal(def.weight()) : null);
        item.setSeason(def.season());
        item.setScene(def.scene());
        item.setComfortTemperature(def.comfortTemp());
        item.setMaxUsers(def.maxUsers());
        item.setStock(50);
        item.setStatus(1);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item.setIsDeleted(false);
        String desc = GearDescriptionHelper.generate(def.name(), def.brand(), def.category(),
                def.weight(), def.season(), def.scene(), def.comfortTemp(), def.maxUsers(), def.tags());
        item.setDescription(desc);
        return item;
    }

    private void linkTags(Long gearId, Map<String, Long> tagMap, LocalDateTime now, String... tagNames) {
        for (String name : tagNames) {
            for (String type : new String[]{"FEATURE", "SCENE", "SEASON", "BUDGET", "MATERIAL", "PERSON"}) {
                Long tagId = tagMap.get(name + ":" + type);
                if (tagId != null) {
                    GearItemTag it = new GearItemTag();
                    it.setGearId(gearId);
                    it.setTagId(tagId);
                    it.setCreatedAt(now);
                    itemTagRepository.save(it);
                    break;
                }
            }
        }
    }
}
