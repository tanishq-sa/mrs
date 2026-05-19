package com.lrms.controller;

import com.lrms.entity.MenuItem;
import com.lrms.entity.Order;
import com.lrms.entity.OrderItem;
import com.lrms.entity.RestaurantTable;
import com.lrms.service.RestaurantService;
import com.lrms.service.LodgingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Restaurant", description = "Management of Menu, Tables, and Orders")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final LodgingService lodgingService;

    public RestaurantController(RestaurantService restaurantService, LodgingService lodgingService) {
        this.restaurantService = restaurantService;
        this.lodgingService = lodgingService;
    }

    // --- Menu ---
    @GetMapping("/menu/items")
    public List<MenuItem> getAllMenuItems() { return restaurantService.getAllMenuItems(); }

    @PostMapping("/menu/items")
    public MenuItem createMenuItem(@RequestBody MenuItem item) { return restaurantService.saveMenuItem(item); }

    @PatchMapping("/menu/items/{id}/toggle")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id) {
        restaurantService.toggleAvailability(id);
        return ResponseEntity.ok().build();
    }

    // --- Tables ---
    @GetMapping("/tables")
    public List<RestaurantTable> getAllTables() { return restaurantService.getAllTables(); }

    @PostMapping("/tables")
    public RestaurantTable createTable(@RequestBody RestaurantTable table) { return restaurantService.saveTable(table); }

    // --- Orders ---
    @GetMapping("/orders")
    public List<Order> getAllOrders() { return restaurantService.getAllOrders(); }

    @PostMapping("/orders")
    public Order placeOrder(@RequestBody Order order) {
        if ("ROOM_SERVICE".equals(order.getOrderType()) && order.getBooking() != null) {
            var booking = lodgingService.getBookingById(order.getBooking().getBookingId());
            order.setGuest(booking.getGuest());
        }
        // For DINE_IN: customerName and customerPhone are already on the Order entity
        // No guest entity needed for walk-in customers
        return restaurantService.placeOrder(order);
    }

    @PostMapping("/orders/{id}/items")
    public Order addItem(@PathVariable Long id, @RequestBody OrderItem item) {
        return restaurantService.addItemToOrder(id, item);
    }

    @DeleteMapping("/orders/{id}/items/{itemId}")
    public Order removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        return restaurantService.removeItemFromOrder(id, itemId);
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        restaurantService.updateOrderStatus(id, statusMap.get("status"));
        return ResponseEntity.ok().build();
    }
}
