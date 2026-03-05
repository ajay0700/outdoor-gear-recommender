package com.outdoor.gear.service;

import com.outdoor.gear.dto.TripPlanCreateRequest;
import com.outdoor.gear.dto.TripPlanDto;
import com.outdoor.gear.service.KeywordExtractorService;
import com.outdoor.gear.dto.TripPlanGearDto;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.TripPlan;
import com.outdoor.gear.entity.TripPlanGear;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.TripPlanGearRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TripPlanService {

    private final TripPlanRepository planRepository;
    private final TripPlanGearRepository planGearRepository;
    private final GearItemRepository gearRepository;
    private final KeywordExtractorService keywordExtractor;

    public TripPlanService(TripPlanRepository planRepository,
                          TripPlanGearRepository planGearRepository,
                          GearItemRepository gearRepository,
                          KeywordExtractorService keywordExtractor) {
        this.planRepository = planRepository;
        this.planGearRepository = planGearRepository;
        this.gearRepository = gearRepository;
        this.keywordExtractor = keywordExtractor;
    }

    @Transactional
    public TripPlanDto create(Long userId, TripPlanCreateRequest req) {
        TripPlan plan = new TripPlan();
        plan.setUserId(userId);
        plan.setName(req.name().trim());
        plan.setDestination(req.destination() != null ? req.destination().trim() : null);
        plan.setStartDate(req.startDate());
        plan.setEndDate(req.endDate());
        plan.setDays(req.days());
        plan.setPeopleCount(req.peopleCount());
        plan.setBudget(req.budget());
        String extracted = req.requirementText() != null && !req.requirementText().isBlank()
                ? keywordExtractor.extractKeywords(req.requirementText()) : "";
        plan.setRequirementText(req.requirementText() != null ? req.requirementText().trim() : null);
        plan.setExtractedKeywords(extracted.isEmpty() ? null : extracted);
        String season = req.season() != null && !req.season().isBlank() ? req.season().trim()
                : keywordExtractor.inferSeason(extracted);
        String activityType = req.activityType() != null && !req.activityType().isBlank() ? req.activityType().trim()
                : keywordExtractor.inferActivityType(extracted);
        plan.setSeason(season);
        plan.setActivityType(activityType);
        plan.setDifficultyLevel(req.difficultyLevel());
        plan.setNote(req.note() != null ? req.note().trim() : null);
        plan.setStatus(req.status() != null ? req.status() : 0);
        LocalDateTime now = LocalDateTime.now();
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan = planRepository.save(plan);
        return toDto(plan);
    }

    @Transactional
    public TripPlanDto update(Long planId, Long userId, TripPlanCreateRequest req) {
        TripPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        plan.setName(req.name().trim());
        plan.setDestination(req.destination() != null ? req.destination().trim() : null);
        plan.setStartDate(req.startDate());
        plan.setEndDate(req.endDate());
        plan.setDays(req.days());
        plan.setPeopleCount(req.peopleCount());
        plan.setBudget(req.budget());
        String extracted = req.requirementText() != null && !req.requirementText().isBlank()
                ? keywordExtractor.extractKeywords(req.requirementText()) : "";
        plan.setRequirementText(req.requirementText() != null ? req.requirementText().trim() : null);
        plan.setExtractedKeywords(extracted.isEmpty() ? null : extracted);
        String season = req.season() != null && !req.season().isBlank() ? req.season().trim()
                : keywordExtractor.inferSeason(extracted);
        String activityType = req.activityType() != null && !req.activityType().isBlank() ? req.activityType().trim()
                : keywordExtractor.inferActivityType(extracted);
        plan.setSeason(season);
        plan.setActivityType(activityType);
        plan.setDifficultyLevel(req.difficultyLevel());
        plan.setNote(req.note() != null ? req.note().trim() : null);
        if (req.status() != null) plan.setStatus(req.status());
        plan.setUpdatedAt(LocalDateTime.now());
        plan = planRepository.save(plan);
        return toDto(plan);
    }

    @Transactional
    public void delete(Long planId, Long userId) {
        TripPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        planGearRepository.findByPlanId(planId).forEach(planGearRepository::delete);
        planRepository.delete(plan);
    }

    public TripPlanDto get(Long planId, Long userId) {
        TripPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        return toDto(plan);
    }

    public Page<TripPlanDto> listByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return planRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toDto);
    }

    @Transactional
    public void addGear(Long planId, Long userId, Long gearId, int quantity, String source) {
        TripPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        if (!gearRepository.existsById(gearId)) {
            throw new IllegalArgumentException("装备不存在");
        }
        TripPlanGear pg = new TripPlanGear();
        pg.setPlanId(planId);
        pg.setGearId(gearId);
        pg.setSource(source != null ? source : "MANUAL");
        pg.setQuantity(quantity);
        pg.setCreatedAt(LocalDateTime.now());
        planGearRepository.save(pg);
    }

    @Transactional
    public void removeGear(Long planId, Long userId, Long gearId) {
        TripPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        List<TripPlanGear> toRemove = planGearRepository.findByPlanId(planId).stream()
                .filter(g -> g.getGearId().equals(gearId))
                .toList();
        if (toRemove.isEmpty()) {
            throw new IllegalArgumentException("计划中无此装备");
        }
        toRemove.forEach(planGearRepository::delete);
    }

    private TripPlanDto toDto(TripPlan plan) {
        List<TripPlanGear> gears = planGearRepository.findByPlanId(plan.getId());
        Map<Long, GearItem> gearMap = gearRepository.findAllById(gears.stream().map(TripPlanGear::getGearId).distinct().toList())
                .stream().collect(Collectors.toMap(GearItem::getId, g -> g));
        List<TripPlanGearDto> gearDtos = gears.stream().map(pg -> {
            GearItem g = gearMap.get(pg.getGearId());
            return new TripPlanGearDto(pg.getId(), pg.getPlanId(), pg.getGearId(),
                    g != null ? g.getName() : null, pg.getSource(), pg.getQuantity());
        }).toList();
        return new TripPlanDto(plan.getId(), plan.getUserId(), plan.getName(), plan.getDestination(),
                plan.getStartDate(), plan.getEndDate(), plan.getDays(), plan.getPeopleCount(),
                plan.getBudget(), plan.getSeason(), plan.getActivityType(), plan.getDifficultyLevel(),
                plan.getNote(), plan.getRequirementText(), plan.getExtractedKeywords(),
                plan.getStatus(), plan.getCreatedAt(), plan.getUpdatedAt(), gearDtos);
    }
}
