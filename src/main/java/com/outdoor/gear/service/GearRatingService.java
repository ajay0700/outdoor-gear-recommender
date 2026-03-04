package com.outdoor.gear.service;

import com.outdoor.gear.dto.GearRatingRequest;
import com.outdoor.gear.entity.GearRating;
import com.outdoor.gear.repository.GearRatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GearRatingService {

    private final GearRatingRepository ratingRepository;

    public GearRatingService(GearRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public void saveOrUpdate(Long userId, Long gearId, GearRatingRequest req) {
        GearRating rating = ratingRepository.findByUserIdAndGearId(userId, gearId)
                .orElse(new GearRating());
        rating.setUserId(userId);
        rating.setGearId(gearId);
        rating.setScore(req.score());
        rating.setComment(req.comment() != null ? req.comment().trim() : null);
        rating.setCreatedAt(rating.getCreatedAt() != null ? rating.getCreatedAt() : LocalDateTime.now());
        ratingRepository.save(rating);
    }
}
