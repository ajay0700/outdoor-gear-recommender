package com.outdoor.gear.controller;

import com.outdoor.gear.dto.TripPlanCreateRequest;
import com.outdoor.gear.dto.TripPlanDto;
import com.outdoor.gear.repository.SysUserRepository;
import com.outdoor.gear.service.TripPlanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户出行计划接口
 */
@RestController
@RequestMapping("/api/user/plans")
public class TripPlanController {

    private final TripPlanService tripPlanService;
    private final SysUserRepository userRepository;

    public TripPlanController(TripPlanService tripPlanService, SysUserRepository userRepository) {
        this.tripPlanService = tripPlanService;
        this.userRepository = userRepository;
    }

    private Long getUserId(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")).getId();
    }

    @GetMapping
    public Page<TripPlanDto> list(Authentication auth,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return tripPlanService.listByUser(getUserId(auth), page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripPlanDto> get(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(tripPlanService.get(id, getUserId(auth)));
    }

    @PostMapping
    public ResponseEntity<TripPlanDto> create(@Valid @RequestBody TripPlanCreateRequest request,
                                              Authentication auth) {
        return ResponseEntity.ok(tripPlanService.create(getUserId(auth), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripPlanDto> update(@PathVariable Long id,
                                              @Valid @RequestBody TripPlanCreateRequest request,
                                              Authentication auth) {
        return ResponseEntity.ok(tripPlanService.update(id, getUserId(auth), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id, Authentication auth) {
        tripPlanService.delete(id, getUserId(auth));
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }

    @PostMapping("/{id}/gears")
    public ResponseEntity<Map<String, String>> addGear(@PathVariable Long id,
                                                         @RequestBody Map<String, Object> body,
                                                         Authentication auth) {
        Long gearId = Long.valueOf(body.get("gearId").toString());
        int quantity = body.containsKey("quantity") ? Integer.parseInt(body.get("quantity").toString()) : 1;
        String source = body.containsKey("source") ? body.get("source").toString() : "MANUAL";
        tripPlanService.addGear(id, getUserId(auth), gearId, quantity, source);
        return ResponseEntity.ok(Map.of("message", "added"));
    }

    @DeleteMapping("/{planId}/gears/{gearId}")
    public ResponseEntity<Map<String, String>> removeGear(@PathVariable Long planId,
                                                          @PathVariable Long gearId,
                                                          Authentication auth) {
        tripPlanService.removeGear(planId, getUserId(auth), gearId);
        return ResponseEntity.ok(Map.of("message", "removed"));
    }
}
