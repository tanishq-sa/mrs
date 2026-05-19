package com.lrms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "room_types")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomTypeId;

    @Column(nullable = false, unique = true, length = 50)
    private String typeName;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private Integer maxOccupancy;

    // Getters and Setters
    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public Integer getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(Integer maxOccupancy) { this.maxOccupancy = maxOccupancy; }
}
