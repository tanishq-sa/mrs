package com.lrms.controller;

import com.lrms.entity.HousekeepingTask;
import com.lrms.entity.Staff;
import com.lrms.service.HousekeepingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Staff and Housekeeping management")
public class AdminController {

    private final HousekeepingService housekeepingService;
    private final com.lrms.service.LodgingService lodgingService;
    private final com.lrms.service.RestaurantService restaurantService;
    private final com.lrms.service.ApiUsageService apiUsageService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(HousekeepingService housekeepingService, 
                           com.lrms.service.LodgingService lodgingService,
                           com.lrms.service.RestaurantService restaurantService,
                           com.lrms.service.ApiUsageService apiUsageService,
                           PasswordEncoder passwordEncoder) {
        this.housekeepingService = housekeepingService;
        this.lodgingService = lodgingService;
        this.restaurantService = restaurantService;
        this.apiUsageService = apiUsageService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("availableRooms", lodgingService.countAvailableRooms());
        stats.put("todayBookings", lodgingService.countTodayBookings());
        stats.put("activeOrders", restaurantService.countActiveOrders());
        stats.put("pendingTasks", housekeepingService.countPendingTasks());
        stats.put("totalApiRequests", apiUsageService.getTotalRequests());
        return stats;
    }

    @GetMapping("/external-usage")
    public Map<String, Object> getExternalUsage() {
        Map<String, Object> usage = new HashMap<>();
        usage.put("recent", apiUsageService.getRecentUsage());
        usage.put("stats", apiUsageService.getUsageStats());
        usage.put("total", apiUsageService.getTotalRequests());
        return usage;
    }

    @GetMapping("/housekeeping")
    public List<HousekeepingTask> getAllTasks() { return housekeepingService.getAllTasks(); }

    @PostMapping("/housekeeping")
    public HousekeepingTask createTask(@RequestBody HousekeepingTask task) { return housekeepingService.createTask(task); }

    @PatchMapping("/housekeeping/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        housekeepingService.updateTaskStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }

    // ── Staff Management ──

    @GetMapping("/staff")
    public List<Staff> getAllStaff() { return housekeepingService.getAllStaff(); }

    @PostMapping("/staff")
    public Staff createStaff(@RequestBody Map<String, String> body) {
        Staff staff = new Staff();
        staff.setFullName(body.get("fullName"));
        staff.setEmail(body.get("email"));
        staff.setPasswordHash(passwordEncoder.encode(body.get("password")));
        staff.setRole(body.get("role"));
        staff.setIsActive(true);
        return housekeepingService.createStaff(staff);
    }

    @PutMapping("/staff/{id}")
    public Staff updateStaffRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return housekeepingService.updateStaffRole(id, body.get("role"));
    }

    @PatchMapping("/staff/{id}/toggle")
    public Staff toggleStaffActive(@PathVariable Long id) {
        return housekeepingService.toggleStaffActive(id);
    }
}
