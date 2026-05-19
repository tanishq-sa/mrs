package com.lrms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest; // nullable for walk-in dine-in customers

    @Column(nullable = false)
    private BigDecimal subtotal;

    private BigDecimal gstAmount = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(length = 20)
    private String paymentMode; // CASH, CARD, UPI, ROOM_CHARGE

    @Column(nullable = false, length = 20)
    private String paymentStatus = "PENDING"; // PENDING, PAID

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getGstAmount() { return gstAmount; }
    public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
