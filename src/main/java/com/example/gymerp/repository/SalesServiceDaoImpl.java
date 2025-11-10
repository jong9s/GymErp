package com.example.gymerp.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.example.gymerp.dto.SalesService;

import lombok.RequiredArgsConstructor;

@Primary
@Repository
@RequiredArgsConstructor
public class SalesServiceDaoImpl implements SalesServiceDao {

    private final SqlSession sqlSession;

    // ===============================
    // [조회]
    // ===============================

    // 전체 서비스 판매 내역 조회
    @Override
    public List<SalesService> selectAllSalesServices() {
        return sqlSession.selectList("SalesServiceMapper.selectAllSalesServices");
    }

    // 단일 서비스 판매 내역 조회
    @Override
    public SalesService selectSalesServiceById(long serviceSalesId) {
        return sqlSession.selectOne("SalesServiceMapper.selectSalesServiceById", serviceSalesId);
    }


    // ===============================
    // [등록 / 수정 / 삭제]
    // ===============================

    // 서비스 판매 등록
    @Override
    public int insertSalesService(SalesService salesService) {
        return sqlSession.insert("SalesServiceMapper.insertSalesService", salesService);
    }

    // 서비스 판매 수정
    @Override
    public int updateSalesService(SalesService salesService) {
        return sqlSession.update("SalesServiceMapper.updateSalesService", salesService);
    }

    // 서비스 판매 삭제 (논리 삭제)
    @Override
    public int deleteSalesService(long serviceSalesId) {
        return sqlSession.update("SalesServiceMapper.deleteSalesService", serviceSalesId);
    }

    // PT_LOG 생성 후 refundId 연동
    @Override
    public int updateRefundIdBySalesId(Map<String, Object> param) {
        return sqlSession.update("SalesServiceMapper.updateRefundIdBySalesId", param);
    }


    // ===============================
    // [판매 내역 조회 - 필터 + 스크롤]
    // ===============================

    // 조건에 따른 전체 개수 조회
    @Override
    public int selectSalesServiceCount(Map<String, Object> params) {
        return sqlSession.selectOne("SalesServiceMapper.selectSalesServiceCount", params);
    }

    // 조건 + 페이징(스크롤) 기반 판매 내역 조회
    @Override
    public List<Map<String, Object>> selectPagedSalesServices(Map<String, Object> params) {
        return sqlSession.selectList("SalesServiceMapper.selectPagedSalesServices", params);
    }


    // ===============================
    // [서비스 매출 통계 조회]
    // ===============================

    // 기간 / 품목명 / 회원 / 직원 기준 서비스 매출 합계 조회
    @Override
    public List<Map<String, Object>> selectServiceSalesAnalytics(Map<String, Object> params) {
        return sqlSession.selectList("SalesServiceMapper.selectServiceSalesAnalytics", params);
    }
}
