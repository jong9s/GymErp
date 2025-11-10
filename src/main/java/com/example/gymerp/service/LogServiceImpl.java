package com.example.gymerp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gymerp.dto.PtLogDto;
import com.example.gymerp.dto.VoucherLogDto;
import com.example.gymerp.repository.LogDao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogDao logDao;

    // ===============================
    // [회원권 관련]
    // ===============================

    // 회원권이 현재 유효한지 여부 확인
    @Override
    public boolean isVoucherValid(long memNum) {
        return logDao.checkVoucherValid(memNum) > 0;
    }

    // 특정 회원의 최신 회원권 정보 조회
    @Override
    public VoucherLogDto getVoucherByMember(long memNum) {
        return logDao.selectVoucherByMember(memNum);
    }

    // 회원권 신규 등록 (최초 구매 시)
    @Override
    @Transactional
    public void insertVoucherLog(VoucherLogDto dto) {
        logDao.insertVoucherLog(dto);
    }

    // 기존 회원권의 기간 연장 (또는 만료 후 재시작 포함)
    @Override
    @Transactional
    public void extendVoucherLog(VoucherLogDto dto) {
        logDao.extendVoucherLog(dto);
    }

    // 회원권 부분환불 (남은 기간 단축)
    @Override
    @Transactional
    public void partialRefundVoucherLog(VoucherLogDto dto) {
        logDao.partialRefundVoucherLog(dto);
    }

    // 회원권 전체환불 (미사용 상태에서 endDate 단축)
    @Override
    @Transactional
    public void fullRefundVoucherLog(VoucherLogDto dto) {
        logDao.fullRefundVoucherLog(dto);
    }


    // ===============================
    // [PT 로그 관련]
    // ===============================

    // PT 신규 충전 로그 등록 (PT권 최초 구매 시)
    @Override
    @Transactional
    public void addPtChargeLog(PtLogDto dto) {
        logDao.insertPtChargeLog(dto);
    }

    // PT 연장 (기존 충전 로그의 countChange 누적 업데이트)
    @Override
    @Transactional
    public void extendPtLog(PtLogDto dto) {
        logDao.updatePtChargeCount(dto);
    }

    // PT 부분환불 로그 등록 (사용 중 일부 환불 시)
    @Override
    @Transactional
    public void addPtPartialRefundLog(PtLogDto dto) {
        logDao.insertPtPartialRefundLog(dto);
    }

    // PT 전체환불 로그 등록 (전액 환불 시)
    @Override
    @Transactional
    public void addPtFullRefundLog(PtLogDto dto) {
        logDao.insertPtFullRefundLog(dto);
    }

    // 특정 회원의 남은 PT 횟수 조회
    @Override
    public int getRemainingPtCount(long memNum) {
        return logDao.selectRemainingPtCount(memNum);
    }

    // 판매내역(salesId) 기준 PT 충전 로그 조회 (수정 시 기준 데이터로 사용)
    @Override
    public PtLogDto getPtLogBySalesId(long salesId) {
        return logDao.getPtLogBySalesId(salesId);
    }

    // 환불 ID(refundId) 기준 PT 로그 조회
    @Override
    public PtLogDto getPtLogByRefundId(long refundId) {
        return logDao.selectPtLogByUsageId(refundId);
    }

    // 기존 PT 충전 로그의 횟수 수정 (연장 처리 전용)
    @Override
    @Transactional
    public void updatePtChargeCount(PtLogDto dto) {
        logDao.updatePtChargeCount(dto);
    }
}
