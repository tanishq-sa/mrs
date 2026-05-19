package com.lrms.controller;

import com.lrms.entity.Booking;
import com.lrms.entity.Order;
import com.lrms.service.ApiUsageService;
import com.lrms.service.LodgingService;
import com.lrms.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/external")
public class ThirdPartyController {

    private final LodgingService lodgingService;
    private final RestaurantService restaurantService;
    private final ApiUsageService apiUsageService;

    public ThirdPartyController(LodgingService lodgingService, RestaurantService restaurantService, ApiUsageService apiUsageService) {
        this.lodgingService = lodgingService;
        this.restaurantService = restaurantService;
        this.apiUsageService = apiUsageService;
    }

    // --- Lodging (e.g., MakeMyTrip) ---
    @GetMapping("/lodging/bookings")
    public ResponseEntity<List<Booking>> listBookings(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner) {
        try {
            List<Booking> bookings = lodgingService.getAllBookings();
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings", "LIST", "SUCCESS");
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings", "LIST", "FAILURE");
            throw e;
        }
    }

    @PostMapping("/lodging/bookings")
    public ResponseEntity<Booking> createBooking(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner, @RequestBody Booking booking) {
        try {
            Booking created = lodgingService.createBooking(booking);
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings", "CREATE", "SUCCESS");
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings", "CREATE", "FAILURE");
            throw e;
        }
    }

    @PutMapping("/lodging/bookings/{id}")
    public ResponseEntity<Booking> updateBooking(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner, @PathVariable Long id, @RequestBody Booking booking) {
        try {
            Booking updated = lodgingService.updateBooking(id, booking);
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings/" + id, "UPDATE", "SUCCESS");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings/" + id, "UPDATE", "FAILURE");
            throw e;
        }
    }

    @DeleteMapping("/lodging/bookings/{id}")
    public ResponseEntity<Void> cancelBooking(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner, @PathVariable Long id) {
        try {
            lodgingService.cancelBooking(id);
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings/" + id, "CANCEL", "SUCCESS");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/lodging/bookings/" + id, "CANCEL", "FAILURE");
            throw e;
        }
    }

    // --- Restaurant (e.g., Zomato/Swiggy) ---
    @GetMapping("/restaurant/orders")
    public ResponseEntity<List<Order>> listOrders(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner) {
        try {
            List<Order> orders = restaurantService.getAllOrders();
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders", "LIST", "SUCCESS");
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders", "LIST", "FAILURE");
            throw e;
        }
    }

    @PostMapping("/restaurant/orders")
    public ResponseEntity<Order> placeOrder(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner, @RequestBody Order order) {
        try {
            Order placed = restaurantService.placeOrder(order);
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders", "CREATE", "SUCCESS");
            return ResponseEntity.ok(placed);
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders", "CREATE", "FAILURE");
            throw e;
        }
    }

    @PatchMapping("/restaurant/orders/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner, @PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("status");
            restaurantService.updateOrderStatus(id, status);
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders/" + id + "/status", "UPDATE", "SUCCESS");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders/" + id + "/status", "UPDATE", "FAILURE");
            throw e;
        }
    }

    @PatchMapping("/restaurant/orders/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@RequestHeader(value = "X-Partner-Name", defaultValue = "Unknown") String partner, @PathVariable Long id) {
        try {
            restaurantService.updateOrderStatus(id, "CANCELLED");
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders/" + id + "/cancel", "CANCEL", "SUCCESS");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            apiUsageService.logUsage(partner, "/api/external/restaurant/orders/" + id + "/cancel", "CANCEL", "FAILURE");
            throw e;
        }
    }
}
