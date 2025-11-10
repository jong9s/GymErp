package com.example.gymerp.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Primary
@Repository
@RequiredArgsConstructor
public class SalesAnalyticsDaoImpl implements SalesAnalyticsDao {

    private final SqlSession sqlSession;


    // ===============================
    // [전체 매출 그래프]
    // - Mapper ID: selectTotalSalesChart
    // - 기준: 서비스 + 실물 상품 매출 합산
    // - 필터: 없음 (전체기간 기준)
    // ===============================
    @Override
    public List<Map<String, Object>> selectTotalSalesChart() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectTotalSalesChart");
    }


    // ===============================
    // [서비스 매출 그래프]
    // - Mapper ID: selectServiceSalesChart
    // - 기준: sales_service
    // - 필터: 없음 (전체기간 기준)
    // ===============================
    @Override
    public List<Map<String, Object>> selectServiceSalesChart() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectServiceSalesChart");
    }


    // ===============================
    // [실물 상품 매출 그래프]
    // - Mapper ID: selectItemSalesChart
    // - 기준: sales_item
    // - 필터: 없음 (전체기간 기준)
    // ===============================
    @Override
    public List<Map<String, Object>> selectItemSalesChart() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectItemSalesChart");
    }


    // ===============================
    // [트레이너 실적 그래프]
    // - Mapper ID: selectTrainerPerformanceChart
    // - 기준: sales_service + employee
    // - 조건: 지난달 기준, 상위 3명
    // ===============================
    @Override
    public List<Map<String, Object>> selectTrainerPerformanceChart() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectTrainerPerformanceChart");
    }


    // ===============================
    // [AI 회원수 예측 결과 저장]
    // - Mapper ID: insertAiMemberPrediction
    // - Flask 예측 결과를 DB에 저장
    // ===============================
    @Override
    public int insertAiMemberPrediction(Map<String, Object> data) {
        return sqlSession.insert("SalesAnalyticsMapper.insertAiMemberPrediction", data);
    }


    // ===============================
    // [AI 회원수 예측 결과 조회]
    // ===============================
    @Override
    public List<Map<String, Object>> selectAiMemberPredictions() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectAiMemberPredictions");
    }


    // ===============================
    // [AI 매출 예측용 과거 데이터 조회]
    // ===============================
    @Override
    public List<Map<String, Object>> selectPastSalesForAi() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectPastSalesForAi");
    }


    // ===============================
    // [AI 매출 예측 결과 저장]
    // ===============================
    @Override
    public int insertAiSalesPrediction(Map<String, Object> data) {
        return sqlSession.insert("SalesAnalyticsMapper.insertAiSalesPrediction", data);
    }


    // ===============================
    // [AI 매출 예측 결과 조회]
    // ===============================
    @Override
    public List<Map<String, Object>> selectAiSalesPredictions() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectSalesWithPrediction");
    }


    // ===============================
    // [AI 회원 예측용 월별 회원 수 집계]
    // - Flask 학습용 데이터용
    // ===============================
    @Override
    public List<Map<String, Object>> selectMemberMonthlyCount(Map<String, Object> params) {
        return sqlSession.selectList("SalesAnalyticsMapper.selectMemberMonthlyCount", params);
    }


    // ===============================
    // [제작년~올해 월별 매출 조회]
    // - 실제 매출 (서비스 + 실물상품)
    // ===============================
    @Override
    public List<Map<String, Object>> selectActualSales3Years() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectActualSales3Years");
    }


    // ===============================
    // [작년~내년 월별 매출 조회 (예측 포함)]
    // - 실제 매출 + 예측 매출 결합
    // ===============================
    @Override
    public List<Map<String, Object>> selectSalesWithPrediction() {
        return sqlSession.selectList("SalesAnalyticsMapper.selectSalesWithPrediction");
    }


    // ===============================
    // [회원권 / PT 잔여현황 통계]
    // ===============================

    // 회원권 유효 회원 비율 조회
    // - 전체 회원권 내역 기준
    // - endDate >= SYSDATE → 유효
    // - endDate < SYSDATE → 만료
    @Override
    public Map<String, Object> selectValidVoucherStats() {
        return sqlSession.selectOne("SalesAnalyticsMapper.selectValidVoucherStats");
    }

    // PT 잔여횟수 보유 회원 비율 조회
    // - 회원 단위 잔여합계 기준
    // - SUM(remainCount) > 0 → 남음
    // - SUM(remainCount) <= 0 → 소진
    @Override
    public Map<String, Object> selectRemainingPtStats() {
        return sqlSession.selectOne("SalesAnalyticsMapper.selectRemainingPtStats");
    }

}
