package com.outdoor.gear.service;

import com.outdoor.gear.dto.UserPreferenceDto;
import com.outdoor.gear.entity.UserPreference;
import com.outdoor.gear.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository repository;

    public UserPreferenceService(UserPreferenceRepository repository) {
        this.repository = repository;
    }

    public UserPreferenceDto getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(p -> UserPreferenceDto.of(
                        p.getSeason(), p.getActivityType(), p.getBudgetMin(), p.getBudgetMax(),
                        p.getPreferredDestinations(), p.getPreferredCategories(), p.getPreferredTags(),
                        p.getDifficultyPreference()))
                .orElse(UserPreferenceDto.of(null, null, null, null, null, null, null, null));
    }

    @Transactional
    public UserPreferenceDto save(Long userId, UserPreferenceDto dto) {
        UserPreference p = repository.findByUserId(userId).orElseGet(() -> {
            UserPreference newP = new UserPreference();
            newP.setUserId(userId);
            newP.setCreatedAt(LocalDateTime.now());
            return newP;
        });
        p.setSeason(dto.season() != null && !dto.season().isBlank() ? dto.season().trim() : null);
        p.setActivityType(dto.activityType() != null && !dto.activityType().isBlank() ? dto.activityType().trim() : null);
        p.setBudgetMin(dto.budgetMin());
        p.setBudgetMax(dto.budgetMax());
        p.setPreferredDestinations(dto.preferredDestinations() != null && !dto.preferredDestinations().isBlank()
                ? dto.preferredDestinations().trim() : null);
        p.setPreferredCategories(dto.preferredCategories() != null && !dto.preferredCategories().isBlank()
                ? dto.preferredCategories().trim() : null);
        p.setPreferredTags(dto.preferredTags() != null && !dto.preferredTags().isBlank()
                ? dto.preferredTags().trim() : null);
        p.setDifficultyPreference(dto.difficultyPreference() != null && !dto.difficultyPreference().isBlank()
                ? dto.difficultyPreference().trim() : null);
        p.setUpdatedAt(LocalDateTime.now());
        repository.save(p);
        return UserPreferenceDto.of(p.getSeason(), p.getActivityType(), p.getBudgetMin(), p.getBudgetMax(),
                p.getPreferredDestinations(), p.getPreferredCategories(), p.getPreferredTags(),
                p.getDifficultyPreference());
    }
}
