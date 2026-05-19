package com.lrms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tableId;

    @Column(nullable = false, unique = true, length = 10)
    private String tableNumber;

    @Column(length = 30)
    private String section; // Indoor, Outdoor, Private

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE"; // AVAILABLE, OCCUPIED, RESERVED, CLEANING

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
