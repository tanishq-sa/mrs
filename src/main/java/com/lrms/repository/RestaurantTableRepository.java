package com.lrms.repository;

import com.lrms.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByStatus(String status);
}
