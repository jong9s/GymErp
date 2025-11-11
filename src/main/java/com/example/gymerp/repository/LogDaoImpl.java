package com.example.gymerp.repository;

import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.example.gymerp.dto.PtLogDto;
import com.example.gymerp.dto.VoucherLogDto;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Primary
@Repository
@RequiredArgsConstructor
public class LogDaoImpl implements LogDao {

    private final SqlSession sqlSession;

    // ===============================
    // [회원권 관련]
    // ===============================

    // 회원권 유효 여부 확인
    @Override
    public int checkVoucherValid(long memNum) {
        return sqlSession.selectOne("LogMapper.checkVoucherValid", memNum);
    }

    // 특정 회원의 최신 회원권 조회
    @Override
    public VoucherLogDto selectVoucherByMember(long memNum) {
        return sqlSession.selectOne("LogMapper.selectVoucherByMember", memNum);
    }

    // 신규 회원권 로그 등록
    @Override
    public int insertVoucherLog(VoucherLogDto dto) {
        return sqlSession.insert("LogMapper.insertVoucherLog", dto);
    }

    // 회원권 기간 연장 (endDate 기준 누적 업데이트)
    @Override
    public int extendVoucherLog(VoucherLogDto dto) {
        return sqlSession.update("LogMapper.extendVoucherLog", dto);
    }

    // 만료된 회원권 재시작 (startDate = SYSDATE)
    @Override
    public int renewVoucherLog(VoucherLogDto dto) {
        return sqlSession.update("LogMapper.renewVoucherLog", dto);
    }

    // 회원권 부분환불 (endDate 단축)
    @Override
    public int partialRefundVoucherLog(VoucherLogDto dto) {
        return sqlSession.update("LogMapper.partialRefundVoucherLog", dto);
    }

    // 회원권 전체환불 (미사용 상태에서 endDate 단축)
    @Override
    public int fullRefundVoucherLog(VoucherLogDto dto) {
        return sqlSession.update("LogMapper.fullRefundVoucherLog", dto);
    }

    // 회원권 기간 N일 연장 (기존 endDate + extendDays)
    @Override
    public int extendVoucherPeriod(Map<String, Object> params) {
        return sqlSession.update("LogMapper.extendVoucherPeriod", params);
    }

    // 회원권 로그 리스트 조회 (기간/회원/페이징)
    @Override
    public List<Map<String, Object>> selectPagedVoucherLogs(Map<String, Object> params) {
        return sqlSession.selectList("LogMapper.selectPagedVoucherLogs", params);
    }

    // 회원권 로그 전체 카운트
    @Override
    public int selectVoucherTotalCount(Map<String, Object> params) {
        return sqlSession.selectOne("LogMapper.selectVoucherTotalCount", params);
    }



    // ===============================
    // [PT 로그 관련]
    // ===============================

    // PT 신규 충전 로그 등록
    @Override
    public int insertPtChargeLog(PtLogDto dto) {
        return sqlSession.insert("LogMapper.insertPtChargeLog", dto);
    }

    // PT 부분환불 로그 등록
    @Override
    public int insertPtPartialRefundLog(PtLogDto dto) {
        return sqlSession.insert("LogMapper.insertPtPartialRefundLog", dto);
    }

    // PT 전체환불 로그 등록
    @Override
    public int insertPtFullRefundLog(PtLogDto dto) {
        return sqlSession.insert("LogMapper.insertPtFullRefundLog", dto);
    }

    // 회원의 남은 PT 횟수 합산 조회
    @Override
    public int selectRemainingPtCount(long memNum) {
        return sqlSession.selectOne("LogMapper.selectRemainingPtCount", memNum);
    }

    // 특정 판매내역(salesId)에 해당하는 PT 충전 로그 조회
    @Override
    public PtLogDto getPtLogBySalesId(long salesId) {
        return sqlSession.selectOne("LogMapper.getPtLogBySalesId", salesId);
    }

    // 특정 환불 ID로 PT 로그 단건 조회
    @Override
    public PtLogDto selectPtLogByUsageId(long usageId) {
        return sqlSession.selectOne("LogMapper.selectPtLogByUsageId", usageId);
    }

    // 기존 충전 로그의 countChange 수정 (연장 시 덮어쓰기)
    @Override
    public int updatePtChargeCount(PtLogDto dto) {
        return sqlSession.update("LogMapper.updatePtChargeCount", dto);
    }

    // 내 차례 도달 여부 확인 (사용 순서 계산)
    @Override
    public int checkPtTurnReached(Map<String, Object> params) {
        return sqlSession.selectOne("LogMapper.checkPtTurnReached", params);
    }

    // 해당 차례 이후 실제 PT 사용 여부 확인
    @Override
    public int getUsedCountBySalesId(Map<String, Object> params) {
        return sqlSession.selectOne("LogMapper.getUsedCountBySalesId", params);
    }

    // PT 로그 리스트 조회 (기간/회원/직원/페이징)
    @Override
    public List<Map<String, Object>> selectPagedPtLogs(Map<String, Object> params) {
        return sqlSession.selectList("LogMapper.selectPagedPtLogs", params);
    }

    // PT 로그 전체 카운트
    @Override
    public int selectPtTotalCount(Map<String, Object> params) {
        return sqlSession.selectOne("LogMapper.selectPtTotalCount", params);
    }



    // ===============================
    // [PT 예약 / 취소 관련]
    // ===============================

    // PT 예약 시 소비 로그 추가 (countChange = -1)
    @Override
    public int insertPtConsumeLog(PtLogDto dto) {
        return sqlSession.insert("LogMapper.insertPtConsumeLog", dto);
    }

    // PT 예약 취소 시 복구 로그 추가 (countChange = +1)
    @Override
    public int insertPtCancelLog(PtLogDto dto) {
        return sqlSession.insert("LogMapper.insertPtCancelLog", dto);
    }

    // 등록번호(regId)로 소비 로그 조회
    @Override
    public PtLogDto selectPtLogByRegId(long regId) {
        return sqlSession.selectOne("LogMapper.selectPtLogByRegId", regId);
    }
}
