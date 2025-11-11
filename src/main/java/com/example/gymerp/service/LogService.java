package com.example.gymerp.service;

import com.example.gymerp.dto.PtLogDto;
import com.example.gymerp.dto.VoucherLogDto;
import java.util.List;
import java.util.Map;

// [LogService 인터페이스]
// - 회원권 및 PT 로그 관련 모든 서비스 로직의 상위 인터페이스
// - SalesServiceServiceImpl, LogController, 단위테스트(SalesServiceServiceTest)와 호환됨
public interface LogService {

    // ===============================
    // [회원권 관련]
    // ===============================

    // 회원권 유효 여부 확인
    boolean isVoucherValid(long memNum);

    // 특정 회원의 회원권 단건 조회
    VoucherLogDto getVoucherByMember(long memNum);

    // 회원권 신규 등록 (최초 구매 시)
    void insertVoucherLog(VoucherLogDto dto);

    // 회원권 기간 연장 (endDate 기준 누적 / 만료 후 재시작 포함)
    void extendVoucherLog(VoucherLogDto dto);

    // 회원권 부분환불 (일수 감소)
    void partialRefundVoucherLog(VoucherLogDto dto);

    // 회원권 전체환불 (미사용 상태에서 endDate 단축)
    void fullRefundVoucherLog(VoucherLogDto dto);

    // 회원권 로그 리스트 조회 (기간/회원/페이징)
    List<Map<String, Object>> getPagedVoucherLogs(Map<String, Object> params);

    // 회원권 로그 전체 카운트
    int getVoucherTotalCount(Map<String, Object> params);



    // ===============================
    // [PT 로그 관련]
    // ===============================

    // PT 신규 충전 로그 등록 (판매 등록 시)
    void addPtChargeLog(PtLogDto dto);

    // PT 연장 처리 (횟수 증가 시 countChange 갱신)
    void extendPtLog(PtLogDto dto);

    // PT 부분환불 로그 등록 (횟수 감소 시)
    void addPtPartialRefundLog(PtLogDto dto);

    // PT 전체환불 로그 등록 (판매 삭제 시)
    void addPtFullRefundLog(PtLogDto dto);

    // 특정 회원의 남은 PT 횟수 조회
    int getRemainingPtCount(long memNum);

    // 특정 판매내역(salesId) 기준 PT 로그 조회 (수정 시 기준)
    PtLogDto getPtLogBySalesId(long salesId);

    // 기존 PT 충전 로그의 countChange 수정 (연장 시 누적 업데이트)
    void updatePtChargeCount(PtLogDto dto);

    /** ✅ 환불 ID(refundId)로 PT 로그 조회 */
    // 환불 ID(refundId)로 PT 로그 조회
    PtLogDto getPtLogByRefundId(long refundId);

    // PT 로그 리스트 조회 (기간/회원/직원/페이징)
    List<Map<String, Object>> getPagedPtLogs(Map<String, Object> params);

    // PT 로그 전체 카운트
    int getPtTotalCount(Map<String, Object> params);
    
  
}
