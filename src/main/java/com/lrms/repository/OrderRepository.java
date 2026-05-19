package com.lrms.repository;

import com.lrms.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String status);
    List<Order> findByBookingBookingId(Long bookingId);
}
