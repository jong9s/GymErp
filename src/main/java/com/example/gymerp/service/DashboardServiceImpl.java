package com.example.gymerp.service;

import com.example.gymerp.repository.DashboardDao;
import com.example.gymerp.dto.DashboardKpiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardDao dashboardDao;

    @Override
    @Transactional(readOnly = true)
    public DashboardKpiDto getKpis() {
        return dashboardDao.selectKpis();
    }
}
