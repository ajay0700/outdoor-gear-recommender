package com.outdoor.gear.controller;

import com.outdoor.gear.entity.OrderItem;
import com.outdoor.gear.entity.OrderMain;
import com.outdoor.gear.entity.RecommendResult;
import com.outdoor.gear.repository.OrderItemRepository;
import com.outdoor.gear.repository.OrderMainRepository;
import com.outdoor.gear.repository.RecommendResultRepository;
import com.outdoor.gear.repository.SysUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/order")
public class TestOrderController {

    private final SysUserRepository userRepository;
    private final OrderMainRepository orderMainRepository;
    private final OrderItemRepository orderItemRepository;
    private final RecommendResultRepository recommendResultRepository;

    public TestOrderController(SysUserRepository userRepository,
                               OrderMainRepository orderMainRepository,
                               OrderItemRepository orderItemRepository,
                               RecommendResultRepository recommendResultRepository) {
        this.userRepository = userRepository;
        this.orderMainRepository = orderMainRepository;
        this.orderItemRepository = orderItemRepository;
        this.recommendResultRepository = recommendResultRepository;
    }

    @GetMapping
    public Map<String, Object> createAndQuery() {
        Map<String, Object> result = new HashMap<>();

        Long userId = 1L;
        if (!userRepository.existsById(userId)) {
            result.put("message", "用户 1 不存在，请先确认 TestDataInitializer 已创建 admin 用户。");
            return result;
        }

        String orderNo = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        OrderMain order = orderMainRepository.findByOrderNo(orderNo).orElseGet(() -> {
            OrderMain o = new OrderMain();
            o.setOrderNo(orderNo);
            o.setUserId(userId);
            o.setTotalAmount(new BigDecimal("299.00"));
            o.setStatus(0);
            o.setCreatedAt(LocalDateTime.now());
            o.setUpdatedAt(LocalDateTime.now());
            return orderMainRepository.save(o);
        });

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        if (items.isEmpty()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setGearId(1L);
            item.setQuantity(1);
            item.setPrice(new BigDecimal("299.00"));
            item.setCreatedAt(LocalDateTime.now());
            orderItemRepository.save(item);
            items = orderItemRepository.findByOrderId(order.getId());
        }

        RecommendResult rr = new RecommendResult();
        rr.setUserId(userId);
        rr.setPlanId(null);
        rr.setAlgorithm("HYBRID");
        rr.setResultJson("[{\"gearId\":1,\"score\":0.95}]");
        rr.setCreatedAt(LocalDateTime.now());
        rr = recommendResultRepository.save(rr);

        result.put("order", order);
        result.put("items", items);
        result.put("recommendResult", rr);
        return result;
    }
}
