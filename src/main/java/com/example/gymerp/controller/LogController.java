package com.example.gymerp.controller;

import org.springframework.web.bind.annotation.*;
import com.example.gymerp.dto.PtLogDto;
import com.example.gymerp.dto.VoucherLogDto;
import com.example.gymerp.service.LogService;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@RestController
@RequestMapping("/v1/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    // ================================
    // [회원권 관련]
    // ================================

    // 회원권 유효 여부 확인
    @GetMapping("/voucher/check")
    public Map<String, Object> checkVoucher(@RequestParam Long memNum) {
        boolean valid = logService.isVoucherValid(memNum);
        return Map.of("memNum", memNum, "isValid", valid);
    }

    // 회원권 단건 조회
    @GetMapping("/voucher/{memNum}")
    public Map<String, Object> getVoucher(@PathVariable Long memNum) {
        VoucherLogDto dto = logService.getVoucherByMember(memNum);
        return Map.of("voucher", dto);
    }

    // 회원권 신규 등록
    @PostMapping("/voucher")
    public Map<String, Object> insertVoucher(@RequestBody VoucherLogDto dto) {
        logService.insertVoucherLog(dto);
        return Map.of("message", "회원권이 등록되었습니다.");
    }

    // 회원권 기간 연장
    @PutMapping("/voucher/extend/{memNum}")
    public Map<String, Object> extendVoucher(@PathVariable Long memNum, @RequestBody VoucherLogDto dto) {
        dto.setMemNum(memNum);
        logService.extendVoucherLog(dto);
        return Map.of("message", "회원권이 연장되었습니다.");
    }

    // 회원권 부분환불
    @PutMapping("/voucher/partialrefund/{memNum}")
    public Map<String, Object> refundVoucher(@PathVariable Long memNum, @RequestBody VoucherLogDto dto) {
        dto.setMemNum(memNum);
        logService.partialRefundVoucherLog(dto);
        return Map.of("message", "회원권이 부분환불되었습니다.");
    }

    // ================================
    // [PT 관련]
    // ================================

    // PT 충전 로그 등록
    @PostMapping("/pt/charge")
    public Map<String, Object> addPtCharge(@RequestBody PtLogDto dto) {
        logService.addPtChargeLog(dto);
        return Map.of("message", "PT 충전 로그가 등록되었습니다.");
    }

    // PT 연장 로그 등록
    @PutMapping("/pt/extend/{usageId}")
    public Map<String, Object> extendPt(@PathVariable Long usageId, @RequestBody PtLogDto dto) {
        dto.setUsageId(usageId);
        logService.extendPtLog(dto);
        return Map.of("message", "PT 로그가 연장되었습니다.");
    }

    // PT 부분환불 로그 등록
    @PostMapping("/pt/refund/partial")
    public Map<String, Object> addPtPartialRefund(@RequestBody PtLogDto dto) {
        logService.addPtPartialRefundLog(dto);
        return Map.of("message", "PT 부분환불 로그가 등록되었습니다.");
    }

    // PT 전체환불 로그 등록
    @PostMapping("/pt/refund/full")
    public Map<String, Object> addPtFullRefund(@RequestBody PtLogDto dto) {
        logService.addPtFullRefundLog(dto);
        return Map.of("message", "PT 전체환불 로그가 등록되었습니다.");
    }

    // 남은 PT 횟수 조회
    @GetMapping("/pt/{memNum}/remaining")
    public Map<String, Object> getRemainingPt(@PathVariable Long memNum) {
        int remaining = logService.getRemainingPtCount(memNum);
        return Map.of("memNum", memNum, "remainingCount", remaining);
    }
}
