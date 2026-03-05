package com.outdoor.gear.service;

import com.outdoor.gear.dto.RecommendConstraint;
import com.outdoor.gear.dto.RecommendRequest;
import com.outdoor.gear.dto.UserPreferenceDto;
import com.outdoor.gear.recommendation.ConstraintFilter;
import com.outdoor.gear.repository.GearCategoryRepository;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.GearItemTagRepository;
import com.outdoor.gear.repository.GearRatingRepository;
import com.outdoor.gear.repository.RecommendResultRepository;
import com.outdoor.gear.repository.TripPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 推荐服务单元测试：候选集为空时返回空列表
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private ConstraintFilter constraintFilter;
    @Mock
    private com.outdoor.gear.recommendation.CollaborativeFilter collaborativeFilter;
    @Mock
    private com.outdoor.gear.recommendation.ContentBasedFilter contentBasedFilter;
    @Mock
    private TripPlanRepository planRepository;
    @Mock
    private GearItemRepository itemRepository;
    @Mock
    private GearCategoryRepository categoryRepository;
    @Mock
    private GearItemTagRepository itemTagRepository;
    @Mock
    private RecommendResultRepository resultRepository;
    @Mock
    private GearRatingRepository ratingRepository;
    @Mock
    private UserPreferenceService preferenceService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void recommend_whenCandidatesEmpty_returnsEmptyList() {
        when(constraintFilter.filter(any(RecommendConstraint.class))).thenReturn(List.of());
        when(preferenceService.getByUserId(any())).thenReturn(
                UserPreferenceDto.of(null, null, null, null, null, null, null, null));

        RecommendRequest request = new RecommendRequest(
                null,
                null,
                new RecommendConstraint(null, null, null, null, null, null, List.of()),
                20
        );
        List<?> result = recommendationService.recommend(request, 1L);

        assertTrue(result.isEmpty());
    }
}
