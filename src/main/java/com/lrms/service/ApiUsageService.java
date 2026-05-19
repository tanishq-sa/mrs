package com.lrms.service;

import com.lrms.entity.ApiUsage;
import com.lrms.repository.ApiUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApiUsageService {

    private final ApiUsageRepository apiUsageRepository;

    public ApiUsageService(ApiUsageRepository apiUsageRepository) {
        this.apiUsageRepository = apiUsageRepository;
    }

    public void logUsage(String partnerName, String endpoint, String method, String status) {
        ApiUsage usage = new ApiUsage(partnerName, endpoint, method, status);
        apiUsageRepository.save(usage);
    }

    public List<ApiUsage> getRecentUsage() {
        return apiUsageRepository.findTop10ByOrderByTimestampDesc();
    }

    public Map<String, Long> getUsageStats() {
        return apiUsageRepository.findAll().stream()
                .collect(Collectors.groupingBy(ApiUsage::getPartnerName, Collectors.counting()));
    }
    
    public long getTotalRequests() {
        return apiUsageRepository.count();
    }
}
