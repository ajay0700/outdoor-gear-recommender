package com.outdoor.gear.controller;

import com.outdoor.gear.entity.GearRating;
import com.outdoor.gear.entity.UserFavorite;
import com.outdoor.gear.repository.GearRatingRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.UserFavoriteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/behavior")
public class TestBehaviorController {

    private final SysUserRepository userRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final GearRatingRepository ratingRepository;

    public TestBehaviorController(SysUserRepository userRepository,
                                  UserFavoriteRepository favoriteRepository,
                                  GearRatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping
    public Map<String, Object> createAndQuery() {
        Map<String, Object> result = new HashMap<>();

        // 简单使用 userId=1, gearId=1 做演示（后续会有真实用户和装备）
        Long userId = 1L;
        Long gearId = 1L;

        if (!userRepository.existsById(userId)) {
            result.put("message", "用户 1 不存在，请先确认 TestDataInitializer 已创建 admin 用户。");
            return result;
        }

        UserFavorite favorite = favoriteRepository
                .findByUserIdAndGearId(userId, gearId)
                .orElseGet(() -> {
                    UserFavorite f = new UserFavorite();
                    f.setUserId(userId);
                    f.setGearId(gearId);
                    f.setCreatedAt(LocalDateTime.now());
                    return favoriteRepository.save(f);
                });

        GearRating rating = ratingRepository
                .findByUserIdAndGearId(userId, gearId)
                .orElseGet(() -> {
                    GearRating r = new GearRating();
                    r.setUserId(userId);
                    r.setGearId(gearId);
                    r.setScore(5);
                    r.setComment("测试评分");
                    r.setCreatedAt(LocalDateTime.now());
                    return ratingRepository.save(r);
                });

        result.put("favorite", favorite);
        result.put("rating", rating);
        return result;
    }
}

