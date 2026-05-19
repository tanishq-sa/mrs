package com.lrms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

    @Column(nullable = false)
    private Integer adults = 1;

    private Integer children = 0;

    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public LocalDateTime getActualCheckIn() { return actualCheckIn; }
    public void setActualCheckIn(LocalDateTime actualCheckIn) { this.actualCheckIn = actualCheckIn; }
    public LocalDateTime getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(LocalDateTime actualCheckOut) { this.actualCheckOut = actualCheckOut; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }
    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
