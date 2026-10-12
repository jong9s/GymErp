-- =====================================================================
-- 성능 측정용 대용량 데이터 (재현 가능하도록 ORA_HASH 기반 결정적 값 사용)
--   employee       50
--   member         100,000   (가입일: 최근 3년)
--   voucher_log    300,000   (회원당 3건, 약 30% 유효)
--   pt_log         1,000,000 (회원 70% 가 PT 이용, 충전/소비/환불 혼합)
--   sales_service  300,000
--   sales_item     300,000
-- 로그인 계정: admin@gym.local / perf1234 (ADMIN), trainer2@gym.local ~ / perf1234
-- =====================================================================

-- 직원
INSERT INTO employee (empNum, empName, gender, empBirth, empPhone, empEmail, password, hireDate, role)
SELECT LEVEL,
       CASE WHEN LEVEL = 1 THEN '관리자' ELSE '트레이너' || LEVEL END,
       CASE WHEN MOD(LEVEL, 2) = 0 THEN '남' ELSE '여' END,
       DATE '1990-01-01' + LEVEL * 30,
       '010-0000-' || LPAD(LEVEL, 4, '0'),
       CASE WHEN LEVEL = 1 THEN 'admin@gym.local' ELSE 'trainer' || LEVEL || '@gym.local' END,
       '$2a$10$inRJJasAGn9/V.22NN2KX.0CvXYbPeDhJEei1cgEb3808B0So0gnq',
       DATE '2020-01-01' + LEVEL * 10,
       CASE WHEN LEVEL = 1 THEN 'ADMIN' ELSE 'EMP' END
FROM dual CONNECT BY LEVEL <= 50;

-- 회원 100,000
INSERT /*+ APPEND */ INTO member (memNum, memName, memGender, memBirthday, memPhone, memEmail, memAddr, memCreated, memUpdated)
SELECT n,
       '회원' || n,
       CASE WHEN MOD(n, 2) = 0 THEN '남' ELSE '여' END,
       DATE '1970-01-01' + ORA_HASH(n, 15000),
       '010-' || LPAD(TRUNC(n / 10000), 4, '0') || '-' || LPAD(MOD(n, 10000), 4, '0'),
       'member' || n || '@test.com',
       '서울시 테스트구 ' || MOD(n, 500) || '번길',
       TRUNC(SYSDATE) - ORA_HASH(n, 1095),
       TRUNC(SYSDATE) - ORA_HASH(n, 30)
FROM (SELECT (a.r - 1) * 1000 + b.r AS n
        FROM (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 100) a,
             (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 1000) b);
COMMIT;

-- 회원권 300,000 (회원당 3건, 마지막 건 기준 약 30% 유효)
INSERT /*+ APPEND */ INTO voucher_log (voucherId, memNum, memberName, startDate, endDate)
SELECT (k - 1) * 100000 + m,
       m,
       '회원' || m,
       TRUNC(SYSDATE) - 1000 + (k - 1) * 300 + ORA_HASH(m, 200),
       TRUNC(SYSDATE) - 1000 + (k - 1) * 300 + ORA_HASH(m, 200) + 30 * (1 + ORA_HASH(m * k, 11))
FROM (SELECT (a.r - 1) * 1000 + b.r AS m
        FROM (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 100) a,
             (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 1000) b),
     (SELECT LEVEL k FROM dual CONNECT BY LEVEL <= 3);
COMMIT;

-- PT 로그 1,000,000 (회원 1~70,000, 트레이너 2~50)
INSERT /*+ APPEND */ INTO pt_log (usageId, memNum, empNum, status, countChange, createdAt, salesId)
SELECT n,
       1 + ORA_HASH(n, 69999),
       2 + ORA_HASH(n * 7, 48),
       CASE WHEN MOD(n, 10) = 0 THEN '충전' WHEN MOD(n, 97) = 0 THEN '부분환불' ELSE '소비' END,
       CASE WHEN MOD(n, 10) = 0 THEN 10 + 10 * ORA_HASH(n, 2) WHEN MOD(n, 97) = 0 THEN -2 ELSE -1 END,
       TRUNC(SYSDATE) - ORA_HASH(n * 13, 1095) + ORA_HASH(n, 86399) / 86400,
       CASE WHEN MOD(n, 10) = 0 THEN TRUNC(n / 10) END
FROM (SELECT (a.r - 1) * 1000 + b.r AS n
        FROM (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 1000) a,
             (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 1000) b);
COMMIT;

-- 서비스 판매 300,000
INSERT /*+ APPEND */ INTO sales_service (serviceSalesId, empNum, memNum, serviceId, serviceName, baseCount, actualCount,
                                         discount, baseAmount, actualAmount, serviceType, status, createdAt)
SELECT n,
       2 + ORA_HASH(n, 48),
       1 + ORA_HASH(n * 3, 99999),
       1 + MOD(n, 6),
       CASE WHEN MOD(n, 2) = 0 THEN 'PT 10회' ELSE '회원권 30일' END,
       CASE WHEN MOD(n, 2) = 0 THEN 10 ELSE 30 END,
       CASE WHEN MOD(n, 2) = 0 THEN 10 ELSE 30 END,
       0,
       CASE WHEN MOD(n, 2) = 0 THEN 500000 ELSE 100000 END,
       CASE WHEN MOD(n, 2) = 0 THEN 500000 ELSE 100000 END,
       CASE WHEN MOD(n, 2) = 0 THEN 'PT' ELSE 'VOUCHER' END,
       'ACTIVE',
       TRUNC(SYSDATE) - ORA_HASH(n * 17, 1095)
FROM (SELECT (a.r - 1) * 1000 + b.r AS n
        FROM (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 300) a,
             (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 1000) b);
COMMIT;

-- 상품 판매 300,000
INSERT /*+ APPEND */ INTO sales_item (itemSalesId, productId, empNum, productName, quantity, unitPrice, totalAmount,
                                      productType, status, createdAt)
SELECT n,
       1 + MOD(n, 20),
       2 + ORA_HASH(n, 48),
       '상품' || (1 + MOD(n, 20)),
       1 + MOD(n, 3),
       10000,
       10000 * (1 + MOD(n, 3)),
       CASE MOD(n, 3) WHEN 0 THEN 'CLOTHES' WHEN 1 THEN 'DRINK' ELSE 'SUPPLEMENTS' END,
       'ACTIVE',
       TRUNC(SYSDATE) - ORA_HASH(n * 19, 1095)
FROM (SELECT (a.r - 1) * 1000 + b.r AS n
        FROM (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 300) a,
             (SELECT LEVEL r FROM dual CONNECT BY LEVEL <= 1000) b);
COMMIT;

-- 시퀀스를 데이터 이후 번호로 재생성
DROP SEQUENCE employee_seq;
CREATE SEQUENCE employee_seq      START WITH 51;
DROP SEQUENCE member_seq;
CREATE SEQUENCE member_seq        START WITH 100001;
DROP SEQUENCE voucher_log_seq;
CREATE SEQUENCE voucher_log_seq   START WITH 300001;
DROP SEQUENCE pt_log_seq;
CREATE SEQUENCE pt_log_seq        START WITH 1000001;
DROP SEQUENCE sales_service_seq;
CREATE SEQUENCE sales_service_seq START WITH 300001;
DROP SEQUENCE sales_item_seq;
CREATE SEQUENCE sales_item_seq    START WITH 300001;

-- 옵티마이저 통계 수집
BEGIN
  DBMS_STATS.GATHER_SCHEMA_STATS(ownname => USER, cascade => TRUE);
END;
/
