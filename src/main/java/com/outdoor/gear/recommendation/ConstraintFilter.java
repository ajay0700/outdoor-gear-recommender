package com.outdoor.gear.recommendation;

import com.outdoor.gear.dto.RecommendConstraint;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.TripPlan;
import com.outdoor.gear.repository.GearItemRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 5.1 约束过滤（第一层）
 * 根据出行计划或用户输入生成约束，在装备表上做多维度过滤，得到候选集
 */
@Component
public class ConstraintFilter {

    private final GearItemRepository itemRepository;

    public ConstraintFilter(GearItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * 从出行计划提取约束并过滤装备
     */
    public List<GearItem> filterByPlan(TripPlan plan) {
        RecommendConstraint constraint = fromPlan(plan);
        return filter(constraint);
    }

    /**
     * 从约束对象过滤装备
     */
    public List<GearItem> filter(RecommendConstraint constraint) {
        Specification<GearItem> spec = buildSpec(constraint);
        return itemRepository.findAll(spec);
    }

    /**
     * 从出行计划生成约束
     * 预算：计划预算为整次出行总预算，单件装备价格不超过「预算×1.2」即可，不设最低价
     * （避免预算 2 万时要求单件 1 万以上，导致无匹配装备）
     */
    public RecommendConstraint fromPlan(TripPlan plan) {
        BigDecimal budgetMin = null;
        BigDecimal budgetMax = null;
        if (plan.getBudget() != null && plan.getBudget().compareTo(BigDecimal.ZERO) > 0) {
            budgetMax = plan.getBudget().multiply(BigDecimal.valueOf(RecommendConstraint.BUDGET_BUFFER));
        }
        return new RecommendConstraint(
                plan.getSeason(),
                budgetMin,
                budgetMax,
                plan.getActivityType(),
                plan.getPeopleCount(),
                null,
                List.of()
        );
    }

    private Specification<GearItem> buildSpec(RecommendConstraint c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), 1));
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (c.season() != null && !c.season().isBlank()) {
                predicates.add(cb.or(
                        cb.isNull(root.get("season")),
                        cb.like(root.get("season"), "%" + c.season().trim() + "%")
                ));
            }
            if (c.budgetMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), c.budgetMin()));
            }
            if (c.budgetMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), c.budgetMax()));
            }
            if (c.scene() != null && !c.scene().isBlank()) {
                predicates.add(cb.or(
                        cb.isNull(root.get("scene")),
                        cb.like(root.get("scene"), "%" + c.scene().trim() + "%")
                ));
            }
            if (c.peopleCount() != null && c.peopleCount() > 0) {
                predicates.add(cb.or(
                        cb.isNull(root.get("maxUsers")),
                        cb.greaterThanOrEqualTo(root.get("maxUsers"), c.peopleCount())
                ));
            }
            if (c.categoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), c.categoryId()));
            }
            if (c.excludeGearIds() != null && !c.excludeGearIds().isEmpty()) {
                predicates.add(cb.not(root.get("id").in(c.excludeGearIds())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
