package com.lrms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "housekeeping_tasks")
public class HousekeepingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private RestaurantTable restaurantTable;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private Staff assignedTo;

    @Column(nullable = false, length = 30)
    private String taskType; // CLEANING, MAINTENANCE, INSPECTION

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, APPROVED

    @Column(length = 10)
    private String priority = "NORMAL"; // NORMAL, HIGH, URGENT

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    // Getters and Setters
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public RestaurantTable getRestaurantTable() { return restaurantTable; }
    public void setRestaurantTable(RestaurantTable restaurantTable) { this.restaurantTable = restaurantTable; }
    public Staff getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Staff assignedTo) { this.assignedTo = assignedTo; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
