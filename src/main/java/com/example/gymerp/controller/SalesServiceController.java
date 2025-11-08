package com.example.gymerp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import com.example.gymerp.dto.SalesService;
import com.example.gymerp.service.SalesServiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
/*@ConditionalOnProperty(     
    prefix = "feature",
    name = "service-sale",
    havingValue = "true",
    matchIfMissing = false
)*/
public class SalesServiceController {

    private final SalesServiceService salesServiceService;


    /* ===============================
       [1. 조회]
    =============================== */

    // 전체 판매 내역 조회
    @GetMapping("/sales/services")
    public List<SalesService> listSalesServices() {
        return salesServiceService.getAllSalesServices();
    }

    // 단일 판매 내역 조회
    @GetMapping("/sales/services/{id}")
    public SalesService getSalesService(@PathVariable Long id) {
        return salesServiceService.getSalesServiceById(id);
    }

    /* ===============================
       [2. 등록]
    =============================== */

    @PostMapping("/sales/services")
    public Map<String, Object> createSalesService(@RequestBody SalesService salesService) {
        int result = salesServiceService.createSalesService(salesService);
        return Map.of("result", result, "message", "판매 내역이 등록되었습니다.");
    }

    /* ===============================
       [3. 수정]
    =============================== */

    @PutMapping("/sales/services/{id}")
    public Map<String, Object> updateSalesService(@PathVariable Long id,
                                                  @RequestBody SalesService salesService) {
        salesService.setServiceSalesId(id);
        salesService.setStatus("ACTIVE");
        int result = salesServiceService.updateSalesService(salesService);
        return Map.of("result", result, "message", "판매 내역이 수정되었습니다.");
    }

    /* ===============================
       [4. 삭제]
    =============================== */

    @DeleteMapping("/sales/services/{id}")
    public Map<String, Object> deleteSalesService(@PathVariable Long id) {
        int result = salesServiceService.deleteSalesService(id);
        return Map.of("result", result, "message", "판매 내역이 삭제되었습니다.");
    }


    /* ===============================
       [5. 판매 내역 조회 (필터 + 검색 + 스크롤)]
       - 필터: 기간 / 품목명 / 회원 / 직원
       - 페이징: startRow, endRow
    =============================== */
    @GetMapping("/sales/services/paged")
    public Map<String, Object> getPagedSalesServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String serviceNameKeyword,
            @RequestParam(required = false) Integer memNum,
            @RequestParam(required = false) Integer empNum) {

        int startRow = (page - 1) * limit + 1;
        int endRow = page * limit;

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("serviceNameKeyword", serviceNameKeyword);
        params.put("memNum", memNum);
        params.put("empNum", empNum);
        params.put("startRow", startRow);
        params.put("endRow", endRow);

        List<Map<String, Object>> list = salesServiceService.getPagedSalesServices(params);
        int totalCount = salesServiceService.getSalesServiceCount(params);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        result.put("currentPage", page);
        result.put("limit", limit);

        return result;
    }


    /* ===============================
       [6. 서비스 매출 통계 조회]
       - 필터: 기간 / 품목명 / 회원 / 직원
    =============================== */
    @GetMapping("/sales/services/analytics")
    public List<Map<String, Object>> getServiceSalesAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String serviceNameKeyword,
            @RequestParam(required = false) Integer memNum,
            @RequestParam(required = false) Integer empNum) {

        return salesServiceService.getServiceSalesAnalytics(
                startDate, endDate, serviceNameKeyword, memNum, empNum);
    }
}
