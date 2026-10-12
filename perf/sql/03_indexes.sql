-- =====================================================================
-- 성능 개선 인덱스
-- Oracle 은 FK 컬럼에 인덱스를 자동 생성하지 않으므로 조회 패턴 기준으로 직접 추가한다.
-- =====================================================================

-- [회원 목록/상세] 회원별 PT 잔여 합계 + 마지막 담당 트레이너
--   SUM(countChange), MAX(empNum) KEEP (DENSE_RANK LAST ORDER BY createdAt, usageId)
--   → 필요한 컬럼을 모두 담은 커버링 인덱스: 테이블 접근 없이 인덱스 범위 스캔만으로 계산
CREATE INDEX ix_ptlog_mem_cover ON pt_log (memNum, createdAt, usageId, empNum, countChange);

-- [PT 로그 목록] 트레이너 필터 / 기간 필터 (+ FK 인덱스 역할)
CREATE INDEX ix_ptlog_emp ON pt_log (empNum, usageId);
CREATE INDEX ix_ptlog_created ON pt_log (createdAt);

-- [회원 목록/상세] 회원별 최신 회원권 (MAX(endDate), KEEP ... startDate)
CREATE INDEX ix_voucher_mem_cover ON voucher_log (memNum, endDate, voucherId, startDate);

-- [회원권 로그 목록] ORDER BY memberName, voucherId 를 인덱스 순서로 읽고 ROWNUM 스톱키로 멈춤
CREATE INDEX ix_voucher_name ON voucher_log (memberName, voucherId);

-- [대시보드] 유효 회원 수: endDate >= 오늘 범위 + DISTINCT memNum
CREATE INDEX ix_voucher_end ON voucher_log (endDate, memNum);

-- [회원 목록] 최근 가입순 정렬(memCreated DESC, memNum DESC)을 인덱스 순서로 읽음 + [대시보드] 이번 달 신규 회원
CREATE INDEX ix_member_created ON member (memCreated, memNum);

-- [대시보드] 이번 달 매출
CREATE INDEX ix_salessvc_created ON sales_service (createdAt, actualAmount);
CREATE INDEX ix_salesitem_created ON sales_item (createdAt, totalAmount);

BEGIN
  DBMS_STATS.GATHER_SCHEMA_STATS(ownname => USER, cascade => TRUE);
END;
/
