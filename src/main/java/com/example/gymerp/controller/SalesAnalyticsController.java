package com.example.gymerp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.example.gymerp.service.SalesAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class SalesAnalyticsController {

    private final SalesAnalyticsService salesAnalyticsService;

    // =========================================================
    // [전체 매출 그래프]
    // - 기준: 서비스 + 실물 상품 매출 합산
    // - 필터 없음 (전체기간 자동)
    // - 그래프: 원형(PieChart)
    // =========================================================
    @GetMapping("/analytics/sales/total")
    public List<Map<String, Object>> getTotalSalesChart() {
        return salesAnalyticsService.getTotalSalesChart();
    }

    // =========================================================
    // [서비스 매출 그래프]
    // - 기준: sales_service
    // - 필터 없음 (전체기간 자동)
    // - 그래프: 원형(PieChart)
    // =========================================================
    @GetMapping("/analytics/sales/service")
    public List<Map<String, Object>> getServiceSalesChart() {
        return salesAnalyticsService.getServiceSalesChart();
    }

    // =========================================================
    // [실물 상품 매출 그래프]
    // - 기준: sales_item
    // - 필터 없음 (전체기간 자동)
    // - 그래프: 원형(PieChart)
    // =========================================================
    @GetMapping("/analytics/sales/item")
    public List<Map<String, Object>> getItemSalesChart() {
        return salesAnalyticsService.getItemSalesChart();
    }

    // =========================================================
    // [트레이너 실적 그래프]
    // - 기준: PT 이용내역(status='소비')
    // - 필터 없음 (기본 = 저번달 기준)
    // - 그래프: 막대 그래프 (BarChart)
    // =========================================================
    @GetMapping("/analytics/trainer/performance")
    public List<Map<String, Object>> getTrainerPerformanceChart() {
        return salesAnalyticsService.getTrainerPerformanceChart();
    }

    // =========================================================
    // [AI 회원수 예측 그래프]
    // - 기준: 올해 전체 (1월~12월)
    // - 테이블: ai_member_prediction
    // - 그래프: 월별 막대그래프 (BarChart)
    // - 필터 없음 (자동 호출)
    // =========================================================
    @GetMapping("/analytics/ai/members")
    public List<Map<String, Object>> getAiMemberPredictionChart() {
        return salesAnalyticsService.getAiMemberPredictionChart();
    }

    // =========================================================
    // [AI 매출 예측 그래프]
    // - 기준: 작년 / 올해 / 내년
    // - 테이블: ai_sales_prediction
    // - 그래프: 꺾은선(LineChart)
    // - 필터 없음 (자동 호출)
    // =========================================================
    @GetMapping("/analytics/ai/sales")
    public List<Map<String, Object>> getAiSalesPredictionChart() {
        return salesAnalyticsService.getAiSalesPredictionChart();
    }

    // =========================================================
    // [제작년~올해 매출 (3년치 실제 매출)]
    // 예: 2023, 2024, 2025
    // =========================================================
    @GetMapping("/history")
    public List<Map<String, Object>> getActualSales3Years() {
        return salesAnalyticsService.getActualSales3Years();
    }

    // =========================================================
    // [작년~내년 매출 (예측 포함)]
    // 예: 2024, 2025, 2026(예측)
    // =========================================================
    @GetMapping("/prediction")
    public List<Map<String, Object>> getSalesWithPrediction() {
        return salesAnalyticsService.getSalesWithPrediction();
    }

    // =========================================================
    // [회원권 유효 회원 통계]
    // - 전체 회원권 내역 보유자 중 endDate >= SYSDATE
    // - 결과: valid_count, expired_count
    // - 그래프: 원형(PieChart)
    // =========================================================
    @GetMapping("/analytics/members/voucher")
    public Map<String, Object> getValidVoucherStats() {
        return salesAnalyticsService.getValidVoucherStats();
    }

    // =========================================================
    // [PT 잔여횟수 회원 통계]
    // - 전체 PT 내역이 있는 회원 중 remainCount > 0
    // - 결과: remaining_count, exhausted_count
    // - 그래프: 원형(PieChart)
    // =========================================================
    @GetMapping("/analytics/members/pt")
    public Map<String, Object> getRemainingPtStats() {
        return salesAnalyticsService.getRemainingPtStats();
    }
}
