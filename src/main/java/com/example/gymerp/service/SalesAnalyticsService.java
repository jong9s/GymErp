package com.example.gymerp.service;

import java.util.List;
import java.util.Map;

public interface SalesAnalyticsService {

    // ===============================
    // [전체 매출 그래프]
    // - 기준: 서비스 + 실물 상품 매출 합산
    // - 필터: 없음 (기본 전체기간)
    // - 결과: label(SERVICE/ITEM), totalSales
    // ===============================
    List<Map<String, Object>> getTotalSalesChart();


    // ===============================
    // [서비스 매출 그래프]
    // - 기준: sales_service
    // - 필터: 없음 (기본 전체기간)
    // - 결과: label(PT/VOUCHER), totalSales
    // ===============================
    List<Map<String, Object>> getServiceSalesChart();


    // ===============================
    // [실물 상품 매출 그래프]
    // - 기준: sales_item
    // - 필터: 없음 (기본 전체기간)
    // - 결과: label(CLOTHES/DRINK/SUPPLEMENTS), totalSales
    // ===============================
    List<Map<String, Object>> getItemSalesChart();


    // ===============================
    // [트레이너 실적 그래프]
    // - 기준: PT 이용내역(status='소비')
    // - 필터: 없음 (기본값 = 저번달)
    // - 결과: trainerName, totalSessions
    // ===============================
    List<Map<String, Object>> getTrainerPerformanceChart();


    // ===============================
    // [AI 회원수 예측 그래프]
    // - 기준: 올해 전체 (1월~12월)
    // - 테이블: ai_member_prediction
    // - 그래프: 월별 막대그래프 (BarChart)
    // ===============================
    List<Map<String, Object>> getAiMemberPredictionChart();


    // ===============================
    // [AI 매출 예측 그래프]
    // - 기준: 과거 3년치 실제 매출 기반 예측
    // - 대상: 작년 / 올해 / 내년
    // - 테이블: ai_sales_prediction
    // - 그래프: 꺾은선 그래프 (LineChart)
    // ===============================
    List<Map<String, Object>> getAiSalesPredictionChart();


    // ===============================
    // [제작년~올해 월별 매출 조회]
    // ===============================
    List<Map<String, Object>> getActualSales3Years();


    // ===============================
    // [작년~내년 월별 매출 조회 (예측 포함)]
    // ===============================
    List<Map<String, Object>> getSalesWithPrediction();


    // ===============================
    // [회원권 / PT 유효 회원 통계]
    // ===============================

    // 회원권 유효회원 비율 (유효 vs 만료)
    Map<String, Object> getValidVoucherStats();

    // PT 잔여횟수 회원 비율 (남음 vs 소진)
    Map<String, Object> getRemainingPtStats();
}
