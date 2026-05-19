package com.lrms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false, length = 50)
    private String categoryName; // Breakfast, Mains, Beverages, Desserts

    @Column(nullable = false, unique = true, length = 100)
    private String itemName;

    @Column(nullable = false)
    private BigDecimal price;

    private Boolean isVeg = false;
    private Boolean isAvailable = true;
    private Integer prepTimeMins;

    // Getters and Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Boolean getIsVeg() { return isVeg; }
    public void setIsVeg(Boolean isVeg) { this.isVeg = isVeg; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public Integer getPrepTimeMins() { return prepTimeMins; }
    public void setPrepTimeMins(Integer prepTimeMins) { this.prepTimeMins = prepTimeMins; }
}
