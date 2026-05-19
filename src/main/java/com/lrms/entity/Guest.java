package com.lrms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestId;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 30)
    private String idProofType;

    @Column(nullable = false, length = 50)
    private String idProofNumber;

    @Column(length = 60)
    private String nationality;

    private Boolean isVip = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public Boolean getIsVip() { return isVip; }
    public void setIsVip(Boolean isVip) { this.isVip = isVip; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
