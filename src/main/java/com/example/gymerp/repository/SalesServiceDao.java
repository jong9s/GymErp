package com.example.gymerp.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import com.example.gymerp.dto.SalesService;

@Mapper
public interface SalesServiceDao {

    // ===============================
    // [조회]
    // ===============================

    // 전체 서비스 판매 내역 조회
    List<SalesService> selectAllSalesServices();

    // 단일 서비스 판매 내역 조회
    SalesService selectSalesServiceById(long serviceSalesId);


    // ===============================
    // [등록 / 수정 / 삭제]
    // ===============================

    // 서비스 판매 등록
    int insertSalesService(SalesService salesService);

    // 서비스 판매 수정
    int updateSalesService(SalesService salesService);

    // 서비스 판매 삭제 (논리 삭제)
    int deleteSalesService(long serviceSalesId);

    // PT_LOG 생성 후 refundId 연동
    int updateRefundIdBySalesId(Map<String, Object> param);


    // ===============================
    // [판매 내역 조회 - 필터 + 스크롤]
    // ===============================

    // 조건에 따른 전체 개수 조회
    int selectSalesServiceCount(Map<String, Object> params);

    // 조건 + 페이징(스크롤) 기반 판매 내역 조회
    List<Map<String, Object>> selectPagedSalesServices(Map<String, Object> params);


    // ===============================
    // [서비스 매출 통계 조회]
    // ===============================

    // 기간 / 품목명 / 회원 / 직원 기준 서비스 매출 합계 조회
    List<Map<String, Object>> selectServiceSalesAnalytics(Map<String, Object> params);
}
