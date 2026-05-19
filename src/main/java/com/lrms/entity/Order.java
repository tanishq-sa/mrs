package com.lrms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private RestaurantTable table; // null for room service

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking; // null for dine-in

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @Column(nullable = false, length = 20)
    private String orderType; // DINE_IN, ROOM_SERVICE

    @Column(nullable = false, length = 20)
    private String status = "PLACED"; // PLACED, PREPARING, READY, SERVED, CANCELLED

    private BigDecimal totalAmount;

    private LocalDateTime placedAt = LocalDateTime.now();

    // Walk-in customer info for dine-in (no guest entity needed)
    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public RestaurantTable getTable() { return table; }
    public void setTable(RestaurantTable table) { this.table = table; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
