package com.example.gymerp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymerp.dto.PtLogDto;
import com.example.gymerp.dto.SalesService;
import com.example.gymerp.dto.VoucherLogDto;
import com.example.gymerp.repository.EmpDao;
import com.example.gymerp.repository.LogDao;
import com.example.gymerp.repository.MemberDao;
import com.example.gymerp.repository.SalesServiceDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesServiceServiceImpl implements SalesServiceService {

    private final SalesServiceDao salesServiceDao;
    private final LogService logService;
    private final MemberDao memberDao;
    private final EmpDao empDao;
    private final LogDao logDao;

    // ===============================
    // [1. 조회]
    // ===============================

    @Override
    public List<SalesService> getAllSalesServices() {
        return salesServiceDao.selectAllSalesServices();
    }

    @Override
    public SalesService getSalesServiceById(long serviceSalesId) {
        return salesServiceDao.selectSalesServiceById(serviceSalesId);
    }

    // ===============================
    // [2. 등록]
    // ===============================
    @Override
    @Transactional
    public int createSalesService(SalesService salesService) {
    	//  0일권 / 0회권 방지 검증
    	if (salesService.getActualCount() == null || salesService.getActualCount() <= 0) {
            throw new IllegalArgumentException("회원권 또는 PT 상품은 1 이상이어야 합니다. (0은 등록 불가)");
        }
    	
    	String memberName = memberDao.selectMemberNameById(salesService.getMemNum().intValue());
        String trainerName = empDao.selectEmployeeNameById(salesService.getEmpNum().intValue());

        // 판매 내역을 먼저 등록 (시퀀스 생성)
        salesServiceDao.insertSalesService(salesService);

        // 방금 생성된 PK
        Long newSalesId = salesService.getServiceSalesId();
        
        DateTimeFormatter oracleFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if ("VOUCHER".equalsIgnoreCase(salesService.getServiceType())) {
            // 기존 회원권 조회
            VoucherLogDto existingVoucher = logService.getVoucherByMember(salesService.getMemNum());
            LocalDateTime now = LocalDateTime.now();

            // Case 1. 기존 회원권 없음 → 신규 등록
            if (existingVoucher == null) {
                LocalDateTime startDate = now;
                LocalDateTime endDate = startDate.plusDays(salesService.getActualCount());

                VoucherLogDto newVoucher = VoucherLogDto.builder()
                    .memNum(salesService.getMemNum())
                    .memberName(memberName)
                    .startDate(startDate.format(oracleFmt))
                    .endDate(endDate.format(oracleFmt))
                    .build();

                logService.insertVoucherLog(newVoucher);
                System.out.println("[VOUCHER] 신규 회원권 등록 완료 (" + startDate + " ~ " + endDate + ")");
            }

            // Case 2. 기존 회원권 있음 + 만료됨(endDate < now) → 재시작
            else if (LocalDateTime.parse(
                    existingVoucher.getEndDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).isBefore(now)) {

                LocalDateTime startDate = now;
                LocalDateTime endDate = startDate.plusDays(salesService.getActualCount());

                existingVoucher.setStartDate(startDate.format(oracleFmt));
                existingVoucher.setEndDate(endDate.format(oracleFmt));

                logService.extendVoucherLog(existingVoucher);
                System.out.println("[VOUCHER] 만료 회원권 재시작 (" + startDate + " ~ " + endDate + ")");
            }

            // Case 3. 기존 회원권 있음 + 유효함(endDate ≥ now) → 기존 endDate 기준 연장
            else {
            	LocalDateTime currentEnd = LocalDateTime.parse(
            		    existingVoucher.getEndDate(),
            		    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            		);
                LocalDateTime newEndDate = currentEnd.plusDays(salesService.getActualCount());

                existingVoucher.setEndDate(newEndDate.format(oracleFmt));

                logService.extendVoucherLog(existingVoucher);
                System.out.println("[VOUCHER] 유효 회원권 연장 (" + existingVoucher.getStartDate() + " ~ " + newEndDate + ")");
            }
        }

        else if ("PT".equalsIgnoreCase(salesService.getServiceType())) {
            if (!logService.isVoucherValid(salesService.getMemNum()))
                throw new IllegalStateException("회원권이 유효하지 않아 PT 등록이 불가합니다.");

            PtLogDto ptLog = PtLogDto.builder()
                    .memNum(salesService.getMemNum())
                    .empNum(salesService.getEmpNum())
                    .status("충전")
                    .countChange(Long.valueOf(salesService.getActualCount()))
                    .salesId(newSalesId)
                    .createdAt(LocalDateTime.now())
                    .build();

            logService.addPtChargeLog(ptLog);

            // refundId 연동
            Long refundId = ptLog.getUsageId();
            if (refundId != null) {
                Map<String, Object> param = new HashMap<>();
                param.put("serviceSalesId", newSalesId);
                param.put("refundId", refundId);
                salesServiceDao.updateRefundIdBySalesId(param);
            }
        }

        return 1;
    }

    // ===============================
    // [3. 수정]
    // ===============================
    @Override
    @Transactional
    public int updateSalesService(SalesService salesService) {
        // 기존 판매내역 조회
        SalesService existing = salesServiceDao.selectSalesServiceById(salesService.getServiceSalesId());
        if (existing == null) throw new IllegalArgumentException("해당 판매 내역이 존재하지 않습니다.");
        if (!existing.getServiceType().equalsIgnoreCase(salesService.getServiceType()))
            throw new IllegalStateException("상품 타입(PT/회원권)은 수정할 수 없습니다.");

        System.out.println("=== [DEBUG] updateSalesService() 진입 ===");
        System.out.println("기존 서비스 타입: " + existing.getServiceType());
        System.out.println("기존 판매ID(serviceSalesId): " + existing.getServiceSalesId());

        // A. PT 상품 (횟수 변경)
        if ("PT".equalsIgnoreCase(existing.getServiceType())) {
            int oldCount = existing.getActualCount();
            int newCount = salesService.getActualCount();

            // 동일하면 변경 없음
            //if (oldCount == newCount) return 1;

            // 1. 연장 (횟수 증가)
            if (newCount > oldCount) {
                int addCount = newCount - oldCount;

                PtLogDto baseLog = logDao.getPtLogBySalesId(existing.getServiceSalesId());
                if (baseLog == null || !"충전".equals(baseLog.getStatus())) {
                    throw new IllegalStateException("PT 충전 로그를 찾을 수 없습니다.");
                }

                baseLog.setCountChange((long) newCount);
                logDao.updatePtChargeCount(baseLog);
                System.out.println("[PT] 충전 연장 완료 → +" + addCount + "회");
            }

            // 2. 부분환불 (횟수 감소)
            else if (newCount < oldCount) {
                int refundCount = oldCount - newCount;

                if (newCount < 1)
                    throw new IllegalStateException("PT는 최소 1회 이상 남겨야 합니다.");

                PtLogDto chargeLog = logDao.getPtLogBySalesId(existing.getServiceSalesId());
                if (chargeLog == null)
                    throw new IllegalStateException("기존 충전 로그를 찾을 수 없습니다.");

                Map<String, Object> params = new HashMap<>();
                params.put("memNum", existing.getMemNum());
                params.put("salesId", existing.getServiceSalesId());

                int turnReached = logDao.checkPtTurnReached(params);
                int usedCount = logDao.getUsedCountBySalesId(params); // ✅ 수정됨

                int refundableCount = oldCount - usedCount;
                if (refundCount > refundableCount)
                    throw new IllegalStateException("이미 사용된 PT를 환불할 수 없습니다. (사용: " + usedCount + "회)");

                PtLogDto refundLog = PtLogDto.builder()
                        .memNum(existing.getMemNum())
                        .empNum(existing.getEmpNum())
                        .status("부분환불")
                        .countChange((long) -refundCount)
                        .salesId(existing.getServiceSalesId())
                        .createdAt(LocalDateTime.now())
                        .build();

                logDao.insertPtPartialRefundLog(refundLog);
                System.out.println("[PT] 부분환불 완료 → -" + refundCount + "회");
            }
        }

        // B. VOUCHER 상품 (일수 변경)
        else if ("VOUCHER".equalsIgnoreCase(existing.getServiceType())) {
            VoucherLogDto voucher = logDao.selectVoucherByMember(existing.getMemNum());
            if (voucher == null) throw new IllegalStateException("회원권 정보를 찾을 수 없습니다.");

            int oldDays = existing.getActualCount();
            int newDays = salesService.getActualCount();

            //if (oldDays == newDays) return 1;

            LocalDate now = LocalDate.now();
            LocalDate endDate = LocalDateTime.parse(
                    voucher.getEndDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).toLocalDate();

            // 1. 연장 (일수 증가)
            if (newDays > oldDays) {
                if (endDate.isBefore(now))
                    throw new IllegalStateException("이미 만료된 회원권은 연장할 수 없습니다.");

                int addDays = newDays - oldDays;
                LocalDate newEndDate = endDate.plusDays(addDays);
                voucher.setEndDate(newEndDate.toString());
                logDao.extendVoucherLog(voucher);
                System.out.println("[VOUCHER] 연장 완료 → +" + addDays + "일");
            }

            // 2. 부분환불 (일수 감소)
            else if (newDays < oldDays) {
                int refundDays = oldDays - newDays;

                LocalDate startDate = LocalDateTime.parse(
                        voucher.getStartDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).toLocalDate();

                LocalDate newEndDate = endDate.minusDays(refundDays);

                if (endDate.isBefore(now)) {
                    throw new IllegalStateException("이미 만료된 회원권은 환불이 불가능합니다.");
                }

                if (newEndDate.isBefore(now)) {
                    throw new IllegalStateException("부분 환불 불가: 환불 결과가 과거일자입니다.");
                }

                if (!newEndDate.isAfter(now)) {
                    throw new IllegalStateException("환불 불가: 회원권은 최소 1일 이상 유지되어야 합니다.");
                }

                DateTimeFormatter oracleFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                voucher.setEndDate(newEndDate.atStartOfDay().format(oracleFmt));
                logDao.partialRefundVoucherLog(voucher);
                System.out.println("[VOUCHER] 부분환불 완료 → -" + refundDays + "일");
            }
        }
        
        existing.setEmpNum(salesService.getEmpNum());
        existing.setServiceName(salesService.getServiceName());
        existing.setBaseCount(salesService.getBaseCount());
        existing.setActualCount(salesService.getActualCount());
        existing.setDiscount(salesService.getDiscount());
        existing.setBaseAmount(salesService.getBaseAmount());
        existing.setActualAmount(salesService.getActualAmount());
        
        System.out.println("=== [DEBUG] updateSalesService() 전달 값 확인 ===");
        System.out.println("serviceSalesId : " + existing.getServiceSalesId());
        System.out.println("serviceName    : " + existing.getServiceName());
        System.out.println("serviceType    : " + existing.getServiceType());
        System.out.println("empNum         : " + existing.getEmpNum());
        System.out.println("baseCount      : " + existing.getBaseCount());
        System.out.println("actualCount    : " + existing.getActualCount());
        System.out.println("baseAmount     : " + existing.getBaseAmount());
        System.out.println("discount       : " + existing.getDiscount());
        System.out.println("actualAmount   : " + existing.getActualAmount());
        System.out.println("memNum         : " + existing.getMemNum());
        System.out.println("updatedAt      : " + LocalDateTime.now());
        System.out.println("===========================================");


        salesServiceDao.updateSalesService(existing);
        return 1;
    }

    // ===============================
    // [4. 삭제]
    // ===============================
    @Override
    @Transactional
    public int deleteSalesService(long serviceSalesId) {
        // 판매내역 조회
        SalesService sale = salesServiceDao.selectSalesServiceById(serviceSalesId);
        if (sale == null)
            throw new IllegalArgumentException("해당 판매 내역이 존재하지 않습니다.");

        // A. PT 상품 삭제 (전체 환불)
        if ("PT".equalsIgnoreCase(sale.getServiceType())) {
            Map<String, Object> params = new HashMap<>();
            params.put("memNum", sale.getMemNum());
            params.put("salesId", sale.getServiceSalesId());
            
            int turnReached = logDao.checkPtTurnReached(params);
            int usedCount = logDao.getUsedCountBySalesId(params); // ✅ 수정됨

            boolean refundable = usedCount == 0;
            if (!refundable)
                throw new IllegalStateException("이미 사용된 PT는 전체 환불(삭제)이 불가능합니다.");

            PtLogDto refundLog = PtLogDto.builder()
                    .memNum(sale.getMemNum())
                    .empNum(sale.getEmpNum())
                    .status("전체환불")
                    .countChange(-sale.getActualCount().longValue())
                    .salesId(sale.getServiceSalesId())
                    .createdAt(LocalDateTime.now())
                    .build();

            logDao.insertPtFullRefundLog(refundLog);
            System.out.println("[PT] 전체환불 완료 → -" + sale.getActualCount() + "회");
        }

        // B. 회원권 삭제 (전체 환불)
        else if ("VOUCHER".equalsIgnoreCase(sale.getServiceType())) {
            VoucherLogDto current = logDao.selectVoucherByMember(sale.getMemNum());
            if (current == null)
                throw new IllegalStateException("회원권 정보가 존재하지 않아 환불할 수 없습니다.");

            LocalDate now = LocalDate.now();

            LocalDate startDate = LocalDateTime.parse(
                    current.getStartDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).toLocalDate();

            LocalDate endDate = LocalDateTime.parse(
                    current.getEndDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).toLocalDate();

            int actualCount = sale.getActualCount();
            LocalDate newEndDate = endDate.minusDays(actualCount);

            long remainingDays = endDate.toEpochDay() - now.toEpochDay();
            if (remainingDays < actualCount) {
                throw new IllegalStateException("전체 환불 불가: 이미 사용한 내역이 존재합니다.");
            }

            if (!newEndDate.isAfter(now)) {
                throw new IllegalStateException("전체 환불 불가: 환불 결과가 과거일자입니다.");
            }

            DateTimeFormatter oracleFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            current.setEndDate(newEndDate.atStartOfDay().format(oracleFmt));
            logDao.fullRefundVoucherLog(current);

            System.out.println("[VOUCHER] 전체환불 완료 → -" + actualCount + "일");
        }

        salesServiceDao.deleteSalesService(serviceSalesId);
        System.out.println("[SALES] 판매내역 논리삭제 완료 (status='DELETED')");
        return 1;
    }

    // ===============================
    // [5. 내역 조회 (필터 + 스크롤)]
    // ===============================
    @Override
    public int getSalesServiceCount(Map<String, Object> params) {
        return salesServiceDao.selectSalesServiceCount(params);
    }

    @Override
    public List<Map<String, Object>> getPagedSalesServices(Map<String, Object> params) {
        return salesServiceDao.selectPagedSalesServices(params);
    }

    // ===============================
    // [6. 서비스 매출 통계 조회]
    // ===============================
    @Override
    public List<Map<String, Object>> getServiceSalesAnalytics(
            String startDate,
            String endDate,
            String serviceNameKeyword,
            Integer memNum,
            Integer empNum
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("serviceNameKeyword", serviceNameKeyword);
        params.put("memNum", memNum);
        params.put("empNum", empNum);
        return salesServiceDao.selectServiceSalesAnalytics(params);
    }
}
