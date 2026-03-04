package com.outdoor.gear.controller;

import com.outdoor.gear.dto.CartItemDto;
import com.outdoor.gear.entity.UserCartItem;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户购物车接口
 */
@RestController
@RequestMapping("/api/user/cart")
public class CartController {

    private final CartService cartService;
    private final SysUserRepository userRepository;
    private final com.outdoor.gear.repository.GearItemRepository gearRepository;

    public CartController(CartService cartService, SysUserRepository userRepository,
                          com.outdoor.gear.repository.GearItemRepository gearRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.gearRepository = gearRepository;
    }

    @GetMapping
    public ResponseEntity<List<CartItemDto>> list(Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        List<UserCartItem> items = cartService.list(userId);
        List<CartItemDto> dtos = items.stream().map(item -> {
            var gear = gearRepository.findById(item.getGearId()).orElse(null);
            return new CartItemDto(item.getId(), item.getGearId(),
                    gear != null ? gear.getName() : null,
                    gear != null ? gear.getPrice() : null,
                    item.getQuantity(), item.getSelected());
        }).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> add(@RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        Long gearId = Long.valueOf(body.get("gearId").toString());
        int quantity = body.containsKey("quantity") ? Integer.parseInt(body.get("quantity").toString()) : 1;
        cartService.add(userId, gearId, quantity);
        return ResponseEntity.ok(Map.of("message", "added"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateQuantity(@PathVariable Long id,
                                                               @RequestBody Map<String, Integer> body,
                                                               Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        Integer quantity = body.get("quantity");
        if (quantity == null) throw new IllegalArgumentException("quantity 不能为空");
        cartService.updateQuantity(userId, id, quantity);
        return ResponseEntity.ok(Map.of("message", "updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> remove(@PathVariable Long id, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
        cartService.remove(userId, id);
        return ResponseEntity.ok(Map.of("message", "removed"));
    }
}
