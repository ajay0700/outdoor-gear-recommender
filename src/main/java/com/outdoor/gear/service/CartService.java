package com.outdoor.gear.service;

import com.outdoor.gear.entity.UserCartItem;
import com.outdoor.gear.repository.UserCartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final UserCartItemRepository cartRepository;

    public CartService(UserCartItemRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional
    public void add(Long userId, Long gearId, int quantity) {
        UserCartItem existing = cartRepository.findByUserId(userId).stream()
                .filter(c -> c.getGearId().equals(gearId))
                .findFirst()
                .orElse(null);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdatedAt(now);
            cartRepository.save(existing);
        } else {
            UserCartItem item = new UserCartItem();
            item.setUserId(userId);
            item.setGearId(gearId);
            item.setQuantity(quantity);
            item.setSelected(true);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            cartRepository.save(item);
        }
    }

    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        UserCartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("购物车项不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        if (quantity <= 0) {
            cartRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(item);
        }
    }

    @Transactional
    public void remove(Long userId, Long cartItemId) {
        UserCartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("购物车项不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        cartRepository.delete(item);
    }

    public List<UserCartItem> list(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public int getQuantity(Long userId, Long gearId) {
        return cartRepository.findByUserId(userId).stream()
                .filter(c -> c.getGearId().equals(gearId))
                .mapToInt(UserCartItem::getQuantity)
                .sum();
    }
}
