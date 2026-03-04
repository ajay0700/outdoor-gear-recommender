package com.outdoor.gear.controller;

import com.outdoor.gear.entity.GearCategory;
import com.outdoor.gear.entity.GearItem;
import com.outdoor.gear.entity.GearTag;
import com.outdoor.gear.repository.GearCategoryRepository;
import com.outdoor.gear.repository.GearItemRepository;
import com.outdoor.gear.repository.GearTagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/gears")
public class TestGearController {

    private final GearCategoryRepository categoryRepository;
    private final GearItemRepository itemRepository;
    private final GearTagRepository tagRepository;

    public TestGearController(GearCategoryRepository categoryRepository,
                              GearItemRepository itemRepository,
                              GearTagRepository tagRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.tagRepository = tagRepository;
    }

    @GetMapping("/categories")
    public List<GearCategory> listCategories() {
        List<GearCategory> list = categoryRepository.findByParentIdOrderBySortOrderAsc(0L);
        if (list.isEmpty()) {
            GearCategory c1 = new GearCategory();
            c1.setName("帐篷");
            c1.setParentId(0L);
            c1.setSortOrder(1);
            c1.setCreatedAt(LocalDateTime.now());
            c1.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(c1);
            GearCategory c2 = new GearCategory();
            c2.setName("睡袋");
            c2.setParentId(0L);
            c2.setSortOrder(2);
            c2.setCreatedAt(LocalDateTime.now());
            c2.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(c2);
            list = categoryRepository.findByParentIdOrderBySortOrderAsc(0L);
        }
        return list;
    }

    @GetMapping("/items")
    public List<GearItem> listItems() {
        List<GearItem> list = itemRepository.findAll();
        if (list.isEmpty()) {
            List<GearCategory> cats = categoryRepository.findByParentIdOrderBySortOrderAsc(0L);
            Long catId = cats.isEmpty() ? 1L : cats.get(0).getId();
            GearItem item = new GearItem();
            item.setName("测试帐篷");
            item.setCategoryId(catId);
            item.setBrand("测试品牌");
            item.setPrice(new BigDecimal("299.00"));
            item.setStock(10);
            item.setStatus(1);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item.setIsDeleted(false);
            itemRepository.save(item);
            list = itemRepository.findAll();
        }
        return list;
    }

    @GetMapping
    public Map<String, Object> summary() {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categoryRepository.findAll());
        result.put("items", itemRepository.findAll());
        result.put("tags", tagRepository.findAll());
        return result;
    }
}
