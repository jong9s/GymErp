-- =====================================================================
-- 성능 측정용 스키마 (Oracle XE 11g)
-- - 측정 대상 API 가 사용하는 테이블만 생성
-- - "개선 전" 상태 재현을 위해 PK 외 인덱스는 만들지 않는다 (원본 table.sql 과 동일)
-- =====================================================================

BEGIN
  FOR t IN (SELECT table_name FROM user_tables
             WHERE table_name IN ('SALES_ITEM','SALES_SERVICE','PT_LOG','VOUCHER_LOG','MEMBER','EMPLOYEE')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences
             WHERE sequence_name IN ('EMPLOYEE_SEQ','MEMBER_SEQ','VOUCHER_LOG_SEQ','PT_LOG_SEQ','SALES_SERVICE_SEQ','SALES_ITEM_SEQ')) LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
  END LOOP;
END;
/

CREATE TABLE employee (
    empNum        NUMBER          PRIMARY KEY,
    empName       VARCHAR2(20)    NOT NULL,
    gender        VARCHAR2(20)    NOT NULL,
    empAddress    VARCHAR2(50),
    empBirth      DATE            NOT NULL,
    empPhone      VARCHAR2(20)    NOT NULL,
    empEmail      VARCHAR2(30)    NOT NULL UNIQUE,
    password      VARCHAR2(100)   NOT NULL,
    hireDate      DATE            NOT NULL,
    fireDate      DATE,
    profileImage  VARCHAR2(255),
    empMemo       VARCHAR2(300),
    role          VARCHAR2(20)    DEFAULT 'EMP' NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'ACTIVE'
);

CREATE TABLE member (
    memNum        NUMBER          PRIMARY KEY,
    memName       VARCHAR2(50)    NOT NULL,
    memGender     VARCHAR2(10)    NOT NULL,
    memBirthday   DATE,
    memPhone      VARCHAR2(20)    NOT NULL UNIQUE,
    memEmail      VARCHAR2(100)   NOT NULL UNIQUE,
    memAddr       VARCHAR2(200),
    memCreated    DATE            DEFAULT SYSDATE,
    memUpdated    DATE            DEFAULT SYSDATE,
    memProfile    VARCHAR2(500),
    memNote       VARCHAR2(500)
);

CREATE TABLE voucher_log (
    voucherId    NUMBER          PRIMARY KEY,
    memNum       NUMBER          NOT NULL,
    memberName   VARCHAR2(100)   NOT NULL,
    startDate    DATE            NOT NULL,
    endDate      DATE            NOT NULL,
    CONSTRAINT fk_voucher_member FOREIGN KEY (memNum) REFERENCES member (memNum)
);

CREATE TABLE pt_log (
    usageId        NUMBER          PRIMARY KEY,
    memNum         NUMBER          NOT NULL,
    empNum         NUMBER          NOT NULL,
    trainerName    VARCHAR2(100),
    memberName     VARCHAR2(100),
    status         VARCHAR2(20)    NOT NULL,
    countChange    NUMBER,
    totalAmount    NUMBER,
    consumeAmount  NUMBER,
    createdAt      DATE            DEFAULT SYSDATE,
    salesId        NUMBER,
    regId          NUMBER,
    CONSTRAINT fk_ptlog_member   FOREIGN KEY (memNum) REFERENCES member(memNum),
    CONSTRAINT fk_ptlog_employee FOREIGN KEY (empNum) REFERENCES employee(empNum)
);

CREATE TABLE sales_service (
    serviceSalesId NUMBER          PRIMARY KEY,
    empNum         NUMBER          NOT NULL,
    memNum         NUMBER          NOT NULL,
    serviceId      NUMBER          NOT NULL,
    serviceName    VARCHAR2(1000)  NOT NULL,
    baseCount      NUMBER          NOT NULL,
    actualCount    NUMBER          NOT NULL,
    discount       NUMBER          DEFAULT 0 NOT NULL,
    baseAmount     NUMBER(12,2)    NOT NULL,
    actualAmount   NUMBER(12,2)    NOT NULL,
    avgPrice       NUMBER(12,2),
    serviceType    VARCHAR2(20)    NOT NULL,
    refundId       NUMBER,
    status         VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL,
    createdAt      DATE            DEFAULT SYSDATE NOT NULL,
    updatedAt      DATE
);

CREATE TABLE sales_item (
    itemSalesId   NUMBER          PRIMARY KEY,
    productId     NUMBER          NOT NULL,
    empNum        NUMBER          NOT NULL,
    productName   VARCHAR2(100)   NOT NULL,
    quantity      NUMBER          NOT NULL,
    unitPrice     NUMBER(12,2)    NOT NULL,
    totalAmount   NUMBER(12,2)    NOT NULL,
    productType   VARCHAR2(20)    NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL,
    createdAt     DATE            DEFAULT SYSDATE NOT NULL,
    updatedAt     DATE
);

CREATE SEQUENCE employee_seq;
CREATE SEQUENCE member_seq;
CREATE SEQUENCE voucher_log_seq;
CREATE SEQUENCE pt_log_seq;
CREATE SEQUENCE sales_service_seq;
CREATE SEQUENCE sales_item_seq;
