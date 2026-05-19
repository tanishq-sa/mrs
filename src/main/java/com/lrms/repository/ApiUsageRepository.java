package com.lrms.repository;

import com.lrms.entity.ApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiUsageRepository extends JpaRepository<ApiUsage, Long> {
    List<ApiUsage> findTop10ByOrderByTimestampDesc();
}
