package com.outdoor.gear.config;

import com.outdoor.gear.entity.GearRating;
import com.outdoor.gear.entity.TripPlan;
import com.outdoor.gear.entity.TripPlanGear;
import com.outdoor.gear.entity.UserCartItem;
import com.outdoor.gear.entity.UserFavorite;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.GearRatingRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.TripPlanGearRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import com.outdoor.gear.repository.UserCartItemRepository;
import com.outdoor.gear.repository.UserFavoriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 初始化测试用户行为数据（收藏、加购、评分、出行计划），用于激活协同过滤与推荐。
 * 3-5 个用户，每人 5-10 件装备，存在重叠以形成相似用户；每人 1 个出行计划。
 */
@Component
@Profile("local")
@Order(3)
public class TestBehaviorDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TestBehaviorDataInitializer.class);

    private final SysUserRepository userRepository;
    private final GearItemRepository gearRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final UserCartItemRepository cartRepository;
    private final GearRatingRepository ratingRepository;
    private final TripPlanRepository planRepository;
    private final TripPlanGearRepository planGearRepository;

    public TestBehaviorDataInitializer(SysUserRepository userRepository,
                                       GearItemRepository gearRepository,
                                       UserFavoriteRepository favoriteRepository,
                                       UserCartItemRepository cartRepository,
                                       GearRatingRepository ratingRepository,
                                       TripPlanRepository planRepository,
                                       TripPlanGearRepository planGearRepository) {
        this.userRepository = userRepository;
        this.gearRepository = gearRepository;
        this.favoriteRepository = favoriteRepository;
        this.cartRepository = cartRepository;
        this.ratingRepository = ratingRepository;
        this.planRepository = planRepository;
        this.planGearRepository = planGearRepository;
    }

    @Override
    public void run(String... args) {
        if (favoriteRepository.count() > 10) {
            log.info("用户行为数据已存在，跳过初始化");
            return;
        }
        List<Long> gearIds = gearRepository.findAll(PageRequest.of(0, 40))
                .stream().map(g -> g.getId()).toList();
        if (gearIds.size() < 10) {
            log.warn("装备数量不足，跳过行为数据初始化");
            return;
        }

        List<Long> userIds = userRepository.findAll().stream()
                .map(u -> u.getId()).toList();
        if (userIds.size() < 2) {
            log.warn("用户数量不足，跳过行为数据初始化");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int favCount = 0, cartCount = 0, ratingCount = 0;

        // 定义各用户喜欢的装备 ID 索引（有重叠）
        // user0(admin): 0-7, user1: 2-9, user2: 4-11, user3: 0-4,14-18, user4: 6-13
        int[][] favoriteRanges = {
                {0, 8},   // user0: gear 0-7
                {2, 10},  // user1: gear 2-9 (overlap: 2-7)
                {4, 12},  // user2: gear 4-11 (overlap: 4-9)
                {0, 5},   // user3: gear 0-4 (overlap with user0)
                {14, 22}, // user3: gear 14-21
                {6, 14}   // user4: gear 6-13 (overlap: 6-11 with user1,2)
        };
        int[] userForRange = {0, 1, 2, 3, 3, 4};

        for (int r = 0; r < favoriteRanges.length; r++) {
            int uidIdx = Math.min(userForRange[r], userIds.size() - 1);
            Long userId = userIds.get(uidIdx);
            int from = favoriteRanges[r][0];
            int to = Math.min(favoriteRanges[r][1], gearIds.size());
            for (int i = from; i < to; i++) {
                Long gearId = gearIds.get(i);
                if (favoriteRepository.findByUserIdAndGearId(userId, gearId).isEmpty()) {
                    UserFavorite f = new UserFavorite();
                    f.setUserId(userId);
                    f.setGearId(gearId);
                    f.setCreatedAt(now);
                    favoriteRepository.save(f);
                    favCount++;
                }
            }
        }

        // 加购：部分用户
        for (int u = 0; u < Math.min(3, userIds.size()); u++) {
            for (int g = u * 3; g < Math.min(u * 3 + 5, gearIds.size()); g++) {
                Long uid = userIds.get(u);
                Long gid = gearIds.get(g);
                if (cartRepository.findByUserId(uid).stream().noneMatch(c -> c.getGearId().equals(gid))) {
                    UserCartItem c = new UserCartItem();
                    c.setUserId(uid);
                    c.setGearId(gid);
                    c.setQuantity(1);
                    c.setSelected(true);
                    c.setCreatedAt(now);
                    c.setUpdatedAt(now);
                    cartRepository.save(c);
                    cartCount++;
                }
            }
        }

        // 评分：4-5 分（协同过滤阈值 4）
        for (int u = 0; u < Math.min(4, userIds.size()); u++) {
            for (int g = u * 2; g < Math.min(u * 2 + 4, gearIds.size()); g++) {
                Long uid = userIds.get(u);
                Long gid = gearIds.get(g);
                if (ratingRepository.findByUserIdAndGearId(uid, gid).isEmpty()) {
                    GearRating r = new GearRating();
                    r.setUserId(uid);
                    r.setGearId(gid);
                    r.setScore(4 + (g % 2));
                    r.setComment("测试评分");
                    r.setCreatedAt(now);
                    ratingRepository.save(r);
                    ratingCount++;
                }
            }
        }

        // 出行计划：每人 1 个，含季节、活动类型、预算等（便于推荐匹配）
        String[][] plans = {
                {"泰山一日游", "泰山", "春", "徒步", "2", "800"},
                {"黄山两日徒步", "黄山", "秋", "徒步", "2", "1200"},
                {"香山秋游", "香山", "秋", "徒步", "3", "500"},
                {"周末露营", "郊区营地", "夏", "露营", "4", "1500"},
                {"稻城亚丁徒步", "稻城", "秋", "徒步", "2", "3000"},
                {"草原自驾露营", "坝上", "夏", "自驾露营", "4", "2000"}
        };
        int planCount = 0;
        for (int i = 0; i < Math.min(plans.length, userIds.size()); i++) {
            Long uid = userIds.get(i);
            if (planRepository.findByUserIdOrderByCreatedAtDesc(uid, PageRequest.of(0, 1)).getContent().isEmpty()) {
                TripPlan p = new TripPlan();
                p.setUserId(uid);
                p.setName(plans[i][0]);
                p.setDestination(plans[i][1]);
                p.setSeason(plans[i][2]);
                p.setActivityType(plans[i][3]);
                p.setPeopleCount(Integer.parseInt(plans[i][4]));
                p.setBudget(new BigDecimal(plans[i][5]));
                p.setStatus(0);
                p.setCreatedAt(now);
                p.setUpdatedAt(now);
                p = planRepository.save(p);
                planCount++;
                // 为计划添加 3-5 件装备（协同过滤会利用计划选品）
                for (int g = i * 2; g < Math.min(i * 2 + 4, gearIds.size()); g++) {
                    TripPlanGear pg = new TripPlanGear();
                    pg.setPlanId(p.getId());
                    pg.setGearId(gearIds.get(g));
                    pg.setQuantity(1);
                    pg.setSource("RECOMMEND");
                    pg.setCreatedAt(now);
                    planGearRepository.save(pg);
                }
            }
        }

        log.info("用户行为数据初始化完成：收藏 {} 条，加购 {} 条，评分 {} 条，计划 {} 个（含选品）", favCount, cartCount, ratingCount, planCount);
    }
}
