package com.outdoor.gear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preference")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 50)
    private String season;

    @Column(length = 100)
    private String activityType;

    @Column(precision = 10, scale = 2)
    private BigDecimal budgetMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal budgetMax;

    @Column(length = 255)
    private String preferredDestinations;

    @Column(length = 255)
    private String preferredCategories;

    @Column(length = 255)
    private String preferredTags;

    @Column(length = 50)
    private String difficultyPreference;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public BigDecimal getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(BigDecimal budgetMin) {
        this.budgetMin = budgetMin;
    }

    public BigDecimal getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getPreferredDestinations() {
        return preferredDestinations;
    }

    public void setPreferredDestinations(String preferredDestinations) {
        this.preferredDestinations = preferredDestinations;
    }

    public String getPreferredCategories() {
        return preferredCategories;
    }

    public void setPreferredCategories(String preferredCategories) {
        this.preferredCategories = preferredCategories;
    }

    public String getPreferredTags() {
        return preferredTags;
    }

    public void setPreferredTags(String preferredTags) {
        this.preferredTags = preferredTags;
    }

    public String getDifficultyPreference() {
        return difficultyPreference;
    }

    public void setDifficultyPreference(String difficultyPreference) {
        this.difficultyPreference = difficultyPreference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
