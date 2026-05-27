package com.lrms.repository;

import com.lrms.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.menuItem.itemName, SUM(oi.quantity) FROM OrderItem oi " +
           "WHERE oi.order.status != 'CANCELLED' " +
           "GROUP BY oi.menuItem.itemName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingItems();
}
