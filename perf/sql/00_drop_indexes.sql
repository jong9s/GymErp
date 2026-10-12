-- 개선 전(인덱스 없음) 상태로 되돌리기
BEGIN
  FOR i IN (SELECT index_name FROM user_indexes
             WHERE index_name IN ('IX_PTLOG_MEM_COVER','IX_PTLOG_EMP','IX_PTLOG_CREATED','IX_VOUCHER_MEM_COVER',
                                  'IX_VOUCHER_NAME','IX_VOUCHER_END','IX_MEMBER_CREATED','IX_SALESSVC_CREATED','IX_SALESITEM_CREATED')) LOOP
    EXECUTE IMMEDIATE 'DROP INDEX ' || i.index_name;
  END LOOP;
END;
/
