package com.outdoor.gear.service;

import com.outdoor.gear.dto.GearCreateRequest;
import com.outdoor.gear.dto.GearDetailDto;
import com.outdoor.gear.dto.GearListItemDto;
import com.outdoor.gear.dto.GearRatingDto;
import com.outdoor.gear.dto.GearUpdateRequest;
import com.outdoor.gear.entity.GearBrowseLog;
import com.outdoor.gear.entity.GearCategory;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.GearItemTag;
import com.outdoor.gear.entity.GearRating;
import com.outdoor.gear.entity.GearTag;
import com.outdoor.gear.entity.SysUser;
import com.outdoor.gear.entity.UserCartItem;
import com.outdoor.gear.entity.UserFavorite;
import com.outdoor.gear.repository.GearBrowseLogRepository;
import com.outdoor.gear.repository.GearCategoryRepository;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.GearItemTagRepository;
import com.outdoor.gear.repository.GearRatingRepository;
import com.outdoor.gear.repository.GearTagRepository;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.repository.UserCartItemRepository;
import com.outdoor.gear.repository.UserFavoriteRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GearService {

    private final GearItemRepository itemRepository;
    private final GearCategoryRepository categoryRepository;
    private final GearTagRepository tagRepository;
    private final GearItemTagRepository itemTagRepository;
    private final GearRatingRepository ratingRepository;
    private final SysUserRepository userRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final UserCartItemRepository cartRepository;
    private final GearBrowseLogRepository browseLogRepository;

    public GearService(GearItemRepository itemRepository,
                       GearCategoryRepository categoryRepository,
                       GearTagRepository tagRepository,
                       GearItemTagRepository itemTagRepository,
                       GearRatingRepository ratingRepository,
                       SysUserRepository userRepository,
                       UserFavoriteRepository favoriteRepository,
                       UserCartItemRepository cartRepository,
                       GearBrowseLogRepository browseLogRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.itemTagRepository = itemTagRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.cartRepository = cartRepository;
        this.browseLogRepository = browseLogRepository;
    }

    /**
     * 管理端：分页列表，多条件筛选
     */
    public Page<GearListItemDto> listForAdmin(Long categoryId, String name, String brand,
                                               BigDecimal priceMin, BigDecimal priceMax,
                                               String season, String scene, Long tagId,
                                               Integer status, int page, int size) {
        Specification<GearItem> spec = buildSpec(categoryId, name, brand, priceMin, priceMax,
                season, scene, tagId, status, false);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GearItem> p = itemRepository.findAll(spec, pageable);
        return p.map(this::toListItemDto);
    }

    /**
     * 用户端：分页列表，仅上架且未删除
     */
    public Page<GearListItemDto> listForUser(Long categoryId, String name, String brand,
                                               BigDecimal priceMin, BigDecimal priceMax,
                                               String season, String scene, Long tagId,
                                               int page, int size) {
        Specification<GearItem> spec = buildSpec(categoryId, name, brand, priceMin, priceMax,
                season, scene, tagId, 1, true);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GearItem> p = itemRepository.findAll(spec, pageable);
        return p.map(this::toListItemDto);
    }

    private Specification<GearItem> buildSpec(Long categoryId, String name, String brand,
                                             BigDecimal priceMin, BigDecimal priceMax,
                                             String season, String scene, Long tagId,
                                             Integer status, boolean userMode) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userMode) {
                predicates.add(cb.equal(root.get("status"), 1));
                predicates.add(cb.equal(root.get("isDeleted"), false));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + name.trim() + "%"));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.like(root.get("brand"), "%" + brand.trim() + "%"));
            }
            if (priceMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), priceMax));
            }
            if (season != null && !season.isBlank()) {
                predicates.add(cb.like(root.get("season"), "%" + season.trim() + "%"));
            }
            if (scene != null && !scene.isBlank()) {
                predicates.add(cb.like(root.get("scene"), "%" + scene.trim() + "%"));
            }
            if (status != null && !userMode) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (tagId != null) {
                List<Long> gearIds = itemTagRepository.findByTagId(tagId).stream()
                        .map(GearItemTag::getGearId).distinct().toList();
                if (gearIds.isEmpty()) {
                    predicates.add(cb.isNull(root.get("id")));
                } else {
                    predicates.add(root.get("id").in(gearIds));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private GearListItemDto toListItemDto(GearItem item) {
        String categoryName = categoryRepository.findById(item.getCategoryId())
                .map(GearCategory::getName).orElse("");
        List<String> tagNames = itemTagRepository.findByGearId(item.getId()).stream()
                .map(it -> tagRepository.findById(it.getTagId()).map(GearTag::getName).orElse(""))
                .filter(n -> !n.isEmpty()).toList();
        return new GearListItemDto(
                item.getId(), item.getName(), item.getCategoryId(), categoryName,
                item.getBrand(), item.getPrice(), item.getWeight(), item.getSeason(), item.getScene(),
                item.getStock(), item.getCoverImage(), item.getStatus(), item.getCreatedAt(), tagNames
        );
    }

    /**
     * 装备详情（用户端可选传入 userId 以返回 isFavorite、cartQuantity）
     */
    public GearDetailDto getDetail(Long gearId, Long userId) {
        GearItem item = itemRepository.findById(gearId)
                .orElseThrow(() -> new IllegalArgumentException("装备不存在"));
        if (item.getIsDeleted()) {
            throw new IllegalArgumentException("装备已下架");
        }
        if (userId != null) {
            GearBrowseLog log = new GearBrowseLog();
            log.setUserId(userId);
            log.setGearId(gearId);
            log.setAction("VIEW");
            log.setCreatedAt(LocalDateTime.now());
            browseLogRepository.save(log);
        }
        String categoryName = categoryRepository.findById(item.getCategoryId())
                .map(GearCategory::getName).orElse("");
        List<String> tagNames = itemTagRepository.findByGearId(item.getId()).stream()
                .map(it -> tagRepository.findById(it.getTagId()).map(GearTag::getName).orElse(""))
                .filter(n -> !n.isEmpty()).toList();

        List<GearRating> ratings = ratingRepository.findByGearIdOrderByCreatedAtDesc(gearId);
        Double avgScore = ratings.isEmpty() ? null : ratings.stream().mapToInt(GearRating::getScore).average().orElse(0);
        Map<Long, SysUser> userMap = ratings.stream().map(GearRating::getUserId).distinct()
                .collect(Collectors.toMap(id -> id, id -> userRepository.findById(id).orElse(null)))
                .entrySet().stream().filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<GearRatingDto> ratingDtos = ratings.stream().limit(20).map(r -> {
            SysUser u = userMap.get(r.getUserId());
            return new GearRatingDto(r.getId(), r.getUserId(),
                    u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "匿名",
                    r.getScore(), r.getComment(), r.getCreatedAt());
        }).toList();

        Boolean isFavorite = userId != null && favoriteRepository.findByUserIdAndGearId(userId, gearId).isPresent();
        Integer cartQuantity = userId != null ? cartRepository.findByUserId(userId).stream()
                .filter(c -> c.getGearId().equals(gearId)).mapToInt(UserCartItem::getQuantity).sum() : 0;

        return new GearDetailDto(
                item.getId(), item.getName(), item.getCategoryId(), categoryName,
                item.getBrand(), item.getPrice(), item.getWeight(), item.getSeason(), item.getScene(),
                item.getComfortTemperature(), item.getMaxUsers(), item.getStock(),
                item.getCoverImage(), item.getImageList(), item.getDescription(), item.getStatus(),
                item.getCreatedAt(), tagNames, avgScore, ratings.size(), ratingDtos, isFavorite, cartQuantity
        );
    }

    /**
     * 管理端：创建装备
     */
    @Transactional
    public GearDetailDto create(GearCreateRequest req, Long createdBy) {
        if (!categoryRepository.existsById(req.categoryId())) {
            throw new IllegalArgumentException("分类不存在");
        }
        GearItem item = new GearItem();
        item.setName(req.name().trim());
        item.setCategoryId(req.categoryId());
        item.setBrand(req.brand() != null ? req.brand().trim() : null);
        item.setPrice(req.price());
        item.setWeight(req.weight());
        item.setSeason(req.season() != null ? req.season().trim() : null);
        item.setScene(req.scene() != null ? req.scene().trim() : null);
        item.setComfortTemperature(req.comfortTemperature() != null ? req.comfortTemperature().trim() : null);
        item.setMaxUsers(req.maxUsers());
        item.setStock(req.stock());
        item.setCoverImage(req.coverImage() != null ? req.coverImage().trim() : null);
        item.setImageList(req.imageList());
        item.setDescription(req.description());
        item.setStatus(req.status());
        item.setCreatedBy(createdBy);
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item.setIsDeleted(false);
        item = itemRepository.save(item);

        if (req.tagIds() != null && !req.tagIds().isEmpty()) {
            for (Long tagId : req.tagIds()) {
                if (tagRepository.existsById(tagId)) {
                    GearItemTag it = new GearItemTag();
                    it.setGearId(item.getId());
                    it.setTagId(tagId);
                    it.setCreatedAt(now);
                    itemTagRepository.save(it);
                }
            }
        }
        return getDetail(item.getId(), null);
    }

    /**
     * 管理端：更新装备
     */
    @Transactional
    public GearDetailDto update(Long id, GearUpdateRequest req) {
        GearItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("装备不存在"));
        if (req.name() != null && !req.name().isBlank()) item.setName(req.name().trim());
        if (req.categoryId() != null) {
            if (!categoryRepository.existsById(req.categoryId())) {
                throw new IllegalArgumentException("分类不存在");
            }
            item.setCategoryId(req.categoryId());
        }
        if (req.brand() != null) item.setBrand(req.brand().trim().isEmpty() ? null : req.brand().trim());
        if (req.price() != null) item.setPrice(req.price());
        if (req.weight() != null) item.setWeight(req.weight());
        if (req.season() != null) item.setSeason(req.season().trim().isEmpty() ? null : req.season().trim());
        if (req.scene() != null) item.setScene(req.scene().trim().isEmpty() ? null : req.scene().trim());
        if (req.comfortTemperature() != null) item.setComfortTemperature(req.comfortTemperature().trim().isEmpty() ? null : req.comfortTemperature().trim());
        if (req.maxUsers() != null) item.setMaxUsers(req.maxUsers());
        if (req.stock() != null) item.setStock(req.stock());
        if (req.coverImage() != null) item.setCoverImage(req.coverImage().trim().isEmpty() ? null : req.coverImage().trim());
        if (req.imageList() != null) item.setImageList(req.imageList());
        if (req.description() != null) item.setDescription(req.description());
        if (req.status() != null) item.setStatus(req.status());
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);

        if (req.tagIds() != null) {
            itemTagRepository.deleteByGearId(id);
            for (Long tagId : req.tagIds()) {
                if (tagRepository.existsById(tagId)) {
                    GearItemTag it = new GearItemTag();
                    it.setGearId(id);
                    it.setTagId(tagId);
                    it.setCreatedAt(LocalDateTime.now());
                    itemTagRepository.save(it);
                }
            }
        }
        return getDetail(id, null);
    }

    /**
     * 管理端：上下架
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("status 必须为 0(下架) 或 1(上架)");
        }
        GearItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("装备不存在"));
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
    }

    /**
     * 管理端：逻辑删除
     */
    @Transactional
    public void delete(Long id) {
        GearItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("装备不存在"));
        item.setIsDeleted(true);
        item.setStatus(0);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
    }

    /**
     * 批量获取装备详情（用于对比等）
     */
    public List<GearDetailDto> getDetailsByIds(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) return List.of();
        Set<Long> idSet = ids.stream().limit(5).collect(Collectors.toSet());
        return idSet.stream()
                .map(id -> {
                    try {
                        return Optional.of(getDetail(id, userId));
                    } catch (Exception e) {
                        return Optional.<GearDetailDto>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
