package com.lrms.service;

import com.lrms.GlobalExceptionHandler;
import com.lrms.entity.*;
import com.lrms.repository.*;
import com.lrms.entity.Booking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
@Transactional
public class RestaurantService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookingRepository bookingRepository;
    private final BillingService billingService;
    private final HousekeepingService housekeepingService;

    public RestaurantService(MenuItemRepository menuItemRepository, 
                             RestaurantTableRepository tableRepository, 
                             OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository,
                             BookingRepository bookingRepository,
                             BillingService billingService,
                             HousekeepingService housekeepingService) {
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookingRepository = bookingRepository;
        this.billingService = billingService;
        this.housekeepingService = housekeepingService;
    }

    // --- Menu ---
    public List<MenuItem> getAllMenuItems() { return menuItemRepository.findAll(); }
    public MenuItem saveMenuItem(MenuItem item) { return menuItemRepository.save(item); }
    public void toggleAvailability(Long id) {
        MenuItem item = menuItemRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Item not found"));
        item.setIsAvailable(!item.getIsAvailable());
        menuItemRepository.save(item);
    }

    // --- Tables ---
    public List<RestaurantTable> getAllTables() { return tableRepository.findAll(); }
    public RestaurantTable saveTable(RestaurantTable table) { return tableRepository.save(table); }
    public void updateTableStatus(Long id, String status) {
        RestaurantTable table = tableRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Table not found"));
        table.setStatus(status);
        tableRepository.save(table);
    }

    // --- Orders ---
    public List<Order> getAllOrders() { return orderRepository.findAll(); }
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Order not found"));
    }

    public Order placeOrder(Order order) {
        // Validate room service: booking must be CHECKED_IN
        if ("ROOM_SERVICE".equals(order.getOrderType())) {
            if (order.getBooking() == null || order.getBooking().getBookingId() == null) {
                throw new IllegalStateException("Room service requires an active booking");
            }
            Booking booking = bookingRepository.findById(order.getBooking().getBookingId())
                    .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Booking not found"));
            if (!"CHECKED_IN".equals(booking.getStatus())) {
                throw new IllegalStateException("Room service is only available for checked-in guests. Booking status: " + booking.getStatus());
            }
            order.setBooking(booking);
            order.setGuest(booking.getGuest());
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            item.setOrder(order);
            BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(itemTotal);
        }
        order.setTotalAmount(total);
        
        if ("DINE_IN".equals(order.getOrderType())) {
            if (order.getTable() != null) updateTableStatus(order.getTable().getTableId(), "OCCUPIED");
        }
        
        return orderRepository.save(order);
    }

    public Order addItemToOrder(Long orderId, OrderItem item) {
        Order order = getOrderById(orderId);
        if (!"PLACED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot add items to order in status: " + order.getStatus());
        }
        item.setOrder(order);
        order.getItems().add(item);
        recalculateOrderTotal(order);
        return orderRepository.save(order);
    }

    public Order removeItemFromOrder(Long orderId, Long itemId) {
        Order order = getOrderById(orderId);
        if (!"PLACED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot remove items from order in status: " + order.getStatus());
        }
        order.getItems().removeIf(item -> item.getOrderItemId().equals(itemId));
        recalculateOrderTotal(order);
        return orderRepository.save(order);
    }

    private void recalculateOrderTotal(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
    }

    public void updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        if ("SERVED".equals(status)) {
            if (order.getTable() != null) {
                updateTableStatus(order.getTable().getTableId(), "CLEANING");
                // Auto-create housekeeping task for table cleaning
                RestaurantTable table = tableRepository.findById(order.getTable().getTableId())
                        .orElse(order.getTable());
                housekeepingService.createTableCleaningTask(table);
            }
            if ("DINE_IN".equals(order.getOrderType())) {
                billingService.generateBillForOrder(id);
            }
        } else if ("CANCELLED".equals(status)) {
            if (order.getTable() != null) {
                updateTableStatus(order.getTable().getTableId(), "AVAILABLE");
            }
        }
        orderRepository.save(order);
    }

    public long countActiveOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> !"SERVED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                .count();
    }

    public List<Map<String, Object>> getTopSellingMenuItems() {
        List<Object[]> raw = orderItemRepository.findTopSellingItems();
        List<Map<String, Object>> list = new ArrayList<>();
        // Cap to top 10 items
        int count = 0;
        for (Object[] row : raw) {
            if (count >= 10) break;
            Map<String, Object> item = new HashMap<>();
            item.put("itemName", row[0]);
            item.put("quantity", row[1]);
            list.add(item);
            count++;
        }
        return list;
    }
}
