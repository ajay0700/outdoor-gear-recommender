package com.outdoor.gear.controller;

import com.outdoor.gear.entity.GearCategory;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.TripPlan;
import com.outdoor.gear.entity.TripPlanGear;
import com.outdoor.gear.repository.GearCategoryRepository;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.TripPlanGearRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/plans")
public class TestTripController {

    private final SysUserRepository userRepository;
    private final TripPlanRepository planRepository;
    private final TripPlanGearRepository planGearRepository;
    private final GearItemRepository itemRepository;
    private final GearCategoryRepository categoryRepository;

    public TestTripController(SysUserRepository userRepository,
                             TripPlanRepository planRepository,
                             TripPlanGearRepository planGearRepository,
                             GearItemRepository itemRepository,
                             GearCategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.planGearRepository = planGearRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public Map<String, Object> createAndQuery() {
        Map<String, Object> result = new HashMap<>();

        Long userId = 1L;
        if (!userRepository.existsById(userId)) {
            result.put("message", "用户 1 不存在，请先确认 TestDataInitializer 已创建 admin 用户。");
            return result;
        }

        List<TripPlan> plans = planRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (plans.isEmpty()) {
            // 确保至少有一个分类和装备，便于关联
            List<GearCategory> cats = categoryRepository.findByParentIdOrderBySortOrderAsc(0L);
            if (cats.isEmpty()) {
                GearCategory c = new GearCategory();
                c.setName("帐篷");
                c.setParentId(0L);
                c.setSortOrder(1);
                c.setCreatedAt(LocalDateTime.now());
                c.setUpdatedAt(LocalDateTime.now());
                categoryRepository.save(c);
                cats = categoryRepository.findByParentIdOrderBySortOrderAsc(0L);
            }
            Long catId = cats.get(0).getId();
            var items = itemRepository.findAll();
            if (items.isEmpty()) {
                GearItem item = new GearItem();
                item.setName("测试帐篷");
                item.setCategoryId(catId);
                item.setPrice(new BigDecimal("299.00"));
                item.setStock(10);
                item.setStatus(1);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                item.setIsDeleted(false);
                itemRepository.save(item);
                items = itemRepository.findAll();
            }

            TripPlan plan = new TripPlan();
            plan.setUserId(userId);
            plan.setName("五一武功山徒步");
            plan.setDestination("武功山");
            plan.setStartDate(LocalDate.of(2026, 5, 1));
            plan.setEndDate(LocalDate.of(2026, 5, 3));
            plan.setDays(3);
            plan.setPeopleCount(2);
            plan.setBudget(new BigDecimal("1500"));
            plan.setSeason("春");
            plan.setActivityType("徒步");
            plan.setStatus(0);
            plan.setCreatedAt(LocalDateTime.now());
            plan.setUpdatedAt(LocalDateTime.now());
            plan = planRepository.save(plan);

            if (!items.isEmpty()) {
                TripPlanGear pg = new TripPlanGear();
                pg.setPlanId(plan.getId());
                pg.setGearId(items.get(0).getId());
                pg.setSource("RECOMMEND");
                pg.setQuantity(1);
                pg.setCreatedAt(LocalDateTime.now());
                planGearRepository.save(pg);
            }
            plans = planRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        result.put("plans", plans);
        if (!plans.isEmpty()) {
            result.put("planGears", planGearRepository.findByPlanId(plans.get(0).getId()));
        }
        return result;
    }
}
