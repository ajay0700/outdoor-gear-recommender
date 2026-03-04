package com.outdoor.gear.recommendation;

import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.GearRating;
import com.outdoor.gear.entity.OrderItem;
import com.outdoor.gear.entity.OrderMain;
import com.outdoor.gear.entity.UserCartItem;
import com.outdoor.gear.entity.UserFavorite;
import com.outdoor.gear.repository.GearRatingRepository;
import com.outdoor.gear.repository.OrderItemRepository;
import com.outdoor.gear.repository.OrderMainRepository;
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
 * 基于用户行为（浏览、收藏、评分、购买）找相似用户，推荐他们喜欢的装备
 */
@Component
public class CollaborativeFilter {

    private static final int RATING_THRESHOLD = 4;

    private final UserFavoriteRepository favoriteRepository;
    private final GearRatingRepository ratingRepository;
    private final UserCartItemRepository cartRepository;
    private final OrderMainRepository orderMainRepository;
    private final OrderItemRepository orderItemRepository;

    public CollaborativeFilter(UserFavoriteRepository favoriteRepository,
                              GearRatingRepository ratingRepository,
                              UserCartItemRepository cartRepository,
                              OrderMainRepository orderMainRepository,
                              OrderItemRepository orderItemRepository) {
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
        this.cartRepository = cartRepository;
        this.orderMainRepository = orderMainRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 获取用户有正向行为的装备 ID 集合（收藏、高评分、加购、购买）
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
        return gearIds;
    }

    /**
     * 获取对某装备有正向行为的用户 ID 集合
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
        return userIds;
    }

    /**
     * 对候选装备按协同过滤得分排序
     * 得分 = 相似用户数（喜欢该装备且与当前用户有共同喜欢的装备）
     */
    public Map<Long, Double> scoreCandidates(List<GearItem> candidates, Long userId) {
        Set<Long> userLiked = getUserLikedGearIds(userId);
        if (userLiked.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, Double> scores = new HashMap<>();
        for (GearItem gear : candidates) {
            Set<Long> usersWhoLiked = getUsersWhoLikedGear(gear.getId());
            long similarCount = usersWhoLiked.stream()
                    .filter(u -> !u.equals(userId))
                    .filter(u -> hasOverlap(u, userLiked))
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
