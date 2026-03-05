package com.outdoor.gear.recommendation;

import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.GearRating;
import com.outdoor.gear.entity.OrderItem;
import com.outdoor.gear.entity.OrderMain;
import com.outdoor.gear.entity.TripPlan;
import com.outdoor.gear.entity.TripPlanGear;
import com.outdoor.gear.entity.UserCartItem;
import com.outdoor.gear.entity.UserFavorite;
import com.outdoor.gear.repository.GearRatingRepository;
import com.outdoor.gear.repository.OrderItemRepository;
import com.outdoor.gear.repository.OrderMainRepository;
import com.outdoor.gear.repository.TripPlanGearRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import com.outdoor.gear.repository.UserCartItemRepository;
import com.outdoor.gear.repository.UserFavoriteRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 5.2 协同过滤（第二层）
 * 基于用户行为（收藏、评分、加购、购买、计划选品）及相似计划找相似用户，推荐他们喜欢的装备
 */
@Component
public class CollaborativeFilter {

    private static final int RATING_THRESHOLD = 4;

    private final UserFavoriteRepository favoriteRepository;
    private final GearRatingRepository ratingRepository;
    private final UserCartItemRepository cartRepository;
    private final OrderMainRepository orderMainRepository;
    private final OrderItemRepository orderItemRepository;
    private final TripPlanGearRepository planGearRepository;
    private final TripPlanRepository planRepository;

    public CollaborativeFilter(UserFavoriteRepository favoriteRepository,
                              GearRatingRepository ratingRepository,
                              UserCartItemRepository cartRepository,
                              OrderMainRepository orderMainRepository,
                              OrderItemRepository orderItemRepository,
                              TripPlanGearRepository planGearRepository,
                              TripPlanRepository planRepository) {
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
        this.cartRepository = cartRepository;
        this.orderMainRepository = orderMainRepository;
        this.orderItemRepository = orderItemRepository;
        this.planGearRepository = planGearRepository;
        this.planRepository = planRepository;
    }

    /**
     * 获取用户有正向行为的装备 ID 集合（收藏、高评分、加购、购买、计划选品）
     */
    public Set<Long> getUserLikedGearIds(Long userId) {
        Set<Long> gearIds = new HashSet<>();
        favoriteRepository.findByUserId(userId).stream().map(UserFavorite::getGearId).forEach(gearIds::add);
        ratingRepository.findByUserId(userId).stream()
                .filter(r -> r.getScore() >= RATING_THRESHOLD)
                .map(GearRating::getGearId)
                .forEach(gearIds::add);
        cartRepository.findByUserId(userId).stream().map(UserCartItem::getGearId).forEach(gearIds::add);
        for (OrderMain order : orderMainRepository.findByUserIdAndStatusGreaterThanEqual(userId, 1)) {
            orderItemRepository.findByOrderId(order.getId()).stream()
                    .map(OrderItem::getGearId)
                    .forEach(gearIds::add);
        }
        // 计划选品：用户加入计划的装备视为强偏好
        planRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .flatMap(p -> planGearRepository.findByPlanId(p.getId()).stream())
                .map(TripPlanGear::getGearId)
                .forEach(gearIds::add);
        return gearIds;
    }

    /**
     * 获取对某装备有正向行为的用户 ID 集合（含计划选品）
     */
    public Set<Long> getUsersWhoLikedGear(Long gearId) {
        Set<Long> userIds = new HashSet<>();
        favoriteRepository.findByGearId(gearId).stream()
                .map(UserFavorite::getUserId)
                .forEach(userIds::add);
        ratingRepository.findByGearIdOrderByCreatedAtDesc(gearId).stream()
                .filter(r -> r.getScore() >= RATING_THRESHOLD)
                .map(GearRating::getUserId)
                .forEach(userIds::add);
        cartRepository.findByGearId(gearId).stream()
                .map(UserCartItem::getUserId)
                .forEach(userIds::add);
        for (OrderItem oi : orderItemRepository.findByGearId(gearId)) {
            orderMainRepository.findById(oi.getOrderId())
                    .filter(o -> o.getStatus() >= 1)
                    .map(OrderMain::getUserId)
                    .ifPresent(userIds::add);
        }
        // 计划选品：将该装备加入计划的用户
        planGearRepository.findByGearId(gearId).stream()
                .map(TripPlanGear::getPlanId)
                .flatMap(pid -> planRepository.findById(pid).stream())
                .map(TripPlan::getUserId)
                .forEach(userIds::add);
        return userIds;
    }

    /**
     * 获取与当前计划相似的其它用户 ID（季节、活动类型一致）
     */
    public Set<Long> getUsersWithSimilarPlans(TripPlan plan) {
        if (plan == null || (plan.getSeason() == null || plan.getSeason().isBlank())
                && (plan.getActivityType() == null || plan.getActivityType().isBlank())) {
            return Set.of();
        }
        Set<Long> userIds = new HashSet<>();
        for (TripPlan p : planRepository.findAll()) {
            if (p.getUserId().equals(plan.getUserId())) continue;
            boolean seasonMatch = plan.getSeason() == null || plan.getSeason().isBlank()
                    || (p.getSeason() != null && p.getSeason().contains(plan.getSeason().trim()));
            boolean sceneMatch = plan.getActivityType() == null || plan.getActivityType().isBlank()
                    || (p.getActivityType() != null && p.getActivityType().contains(plan.getActivityType().trim()));
            if (seasonMatch || sceneMatch) {
                userIds.add(p.getUserId());
            }
        }
        return userIds;
    }

    /**
     * 对候选装备按协同过滤得分排序
     * 相似用户 = 有共同喜欢装备的用户 或 有相似计划的用户
     */
    public Map<Long, Double> scoreCandidates(List<GearItem> candidates, Long userId) {
        return scoreCandidates(candidates, userId, null);
    }

    /**
     * 对候选装备按协同过滤得分排序（可传入当前计划以考虑相似计划用户）
     */
    public Map<Long, Double> scoreCandidates(List<GearItem> candidates, Long userId, TripPlan currentPlan) {
        Set<Long> userLiked = getUserLikedGearIds(userId);
        Set<Long> planSimilarUsers = currentPlan != null ? getUsersWithSimilarPlans(currentPlan) : Set.of();

        Map<Long, Double> scores = new HashMap<>();
        for (GearItem gear : candidates) {
            Set<Long> usersWhoLiked = getUsersWhoLikedGear(gear.getId());
            long similarCount = usersWhoLiked.stream()
                    .filter(u -> !u.equals(userId))
                    .filter(u -> hasOverlap(u, userLiked) || planSimilarUsers.contains(u))
                    .count();
            if (similarCount > 0) {
                scores.put(gear.getId(), (double) similarCount);
            }
        }
        return scores;
    }

    private boolean hasOverlap(Long otherUserId, Set<Long> userLiked) {
        Set<Long> otherLiked = getUserLikedGearIds(otherUserId);
        return otherLiked.stream().anyMatch(userLiked::contains);
    }
}
