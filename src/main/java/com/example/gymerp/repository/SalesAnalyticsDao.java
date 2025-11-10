package com.example.gymerp.repository;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesAnalyticsDao {

    // ===============================
    // [전체 매출 그래프]
    // - Mapper: selectTotalSalesChart
    // - 기준: 서비스 + 실물 상품 매출 합산
    // - 필터: 없음 (전체기간 기준)
    // - 출력: label(SERVICE/ITEM), total_sales
    // ===============================
    List<Map<String, Object>> selectTotalSalesChart();


    // ===============================
    // [서비스 매출 그래프]
    // - Mapper: selectServiceSalesChart
    // - 기준: sales_service
    // - 필터: 없음 (전체기간 기준)
    // - 출력: label(PT/VOUCHER), total_sales
    // ===============================
    List<Map<String, Object>> selectServiceSalesChart();


    // ===============================
    // [실물 상품 매출 그래프]
    // - Mapper: selectItemSalesChart
    // - 기준: sales_item
    // - 필터: 없음 (전체기간 기준)
    // - 출력: label(CLOTHES/DRINK/SUPPLEMENTS), total_sales
    // ===============================
    List<Map<String, Object>> selectItemSalesChart();


    // ===============================
    // [트레이너 실적 그래프]
    // - Mapper: selectTrainerPerformanceChart
    // - 기준: sales_service + employee
    // - 필터: 없음 (지난달 자동)
    // - 출력: label(직원명), total_sales (상위 3명)
    // ===============================
    List<Map<String, Object>> selectTrainerPerformanceChart();


    // ===============================
    // [AI 회원수 예측 결과 저장]
    // ===============================
    int insertAiMemberPrediction(Map<String, Object> data);


    // ===============================
    // [AI 회원수 예측 결과 조회]
    // ===============================
    List<Map<String, Object>> selectAiMemberPredictions();


    // ===============================
    // [AI 매출 예측용 과거 데이터 조회]
    // ===============================
    List<Map<String, Object>> selectPastSalesForAi();


    // ===============================
    // [AI 매출 예측 결과 저장]
    // ===============================
    int insertAiSalesPrediction(Map<String, Object> data);


    // ===============================
    // [AI 매출 예측 결과 조회]
    // ===============================
    List<Map<String, Object>> selectAiSalesPredictions();


    // ===============================
    // [AI 회원 예측용 월별 회원 수 집계]
    // - Flask 학습용 데이터 생성용
    // ===============================
    List<Map<String, Object>> selectMemberMonthlyCount(Map<String, Object> params);


    // ===============================
    // [제작년~올해 월별 매출 조회]
    // - 실제 매출 (서비스 + 실물상품)
    // ===============================
    List<Map<String, Object>> selectActualSales3Years();


    // ===============================
    // [작년~내년 월별 매출 조회 (예측 포함)]
    // - 실제 매출 + 예측 매출 결합
    // ===============================
    List<Map<String, Object>> selectSalesWithPrediction();


    // ===============================
    // [회원권 / PT 잔여현황 통계]
    // ===============================

    // 회원권 유효 회원 비율
    Map<String, Object> selectValidVoucherStats();

    // PT 잔여횟수 보유 회원 비율
    Map<String, Object> selectRemainingPtStats();
}
