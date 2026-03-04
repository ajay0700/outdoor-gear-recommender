package com.outdoor.gear.service;

import com.outdoor.gear.entity.UserFavorite;
import com.outdoor.gear.repository.UserFavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;

    public FavoriteService(UserFavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional
    public void add(Long userId, Long gearId) {
        if (favoriteRepository.findByUserIdAndGearId(userId, gearId).isPresent()) {
            return;
        }
        UserFavorite f = new UserFavorite();
        f.setUserId(userId);
        f.setGearId(gearId);
        f.setCreatedAt(LocalDateTime.now());
        favoriteRepository.save(f);
    }

    @Transactional
    public void remove(Long userId, Long gearId) {
        favoriteRepository.findByUserIdAndGearId(userId, gearId).ifPresent(favoriteRepository::delete);
    }

    public List<Long> getFavoriteGearIds(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(UserFavorite::getGearId)
                .toList();
    }

    public boolean isFavorite(Long userId, Long gearId) {
        return favoriteRepository.findByUserIdAndGearId(userId, gearId).isPresent();
    }
}
