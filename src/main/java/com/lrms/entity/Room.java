package com.lrms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false, unique = true, length = 10)
    private String roomNumber;

    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false, length = 20)
    private String status; // AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE

    private Boolean isActive = true;

    // Getters and Setters
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
