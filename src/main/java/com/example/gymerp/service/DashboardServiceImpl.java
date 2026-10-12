package com.example.gymerp.service;

import com.example.gymerp.repository.DashboardDao;
import com.example.gymerp.dto.DashboardKpiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardDao dashboardDao;

    // KPI 는 실시간 정확도보다 조회 빈도가 훨씬 높아 60초 TTL 캐시 적용 (application.properties)
    @Override
    @Cacheable(cacheNames = "dashboardKpi", key = "'kpi'")
    @Transactional(readOnly = true)
    public DashboardKpiDto getKpis() {
        return dashboardDao.selectKpis();
    }
}
