package com.example.gymerp.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gymerp.dto.SalesItemDto;
import com.example.gymerp.service.SalesItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/v1/sales")
@RequiredArgsConstructor
public class SalesItemController {

    private static final Logger logger = LoggerFactory.getLogger(SalesItemController.class);

    private final SalesItemService salesItemService;
    
    // 상품 등록
    @PostMapping("/products")
    public ResponseEntity<?> addSalesItem(@RequestBody @Valid SalesItemDto dto){
      var createdId = salesItemService.addSalesItem(dto); // 내부에서 재고 검증 & 커스텀 예외 throw
      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
        "message", "판매 내역이 성공적으로 등록되었습니다.",
        "id", createdId
      ));
    }


    // 상품 판매 전체 목록 조회
    @GetMapping("/products") 
    public ResponseEntity<Map<String, Object>> getAllSalesItems(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String productNameKeyword, 
            @RequestParam(required = false) Integer empNum,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> result = salesItemService.getAllSalesItems(startDate, endDate, productNameKeyword, empNum, page, size);
        
        return ResponseEntity.ok(result);
    }

    // 상품 판매 단일 조회
    @GetMapping("/products/{itemSalesId}") 
    public ResponseEntity<SalesItemDto> getSalesItemById(@PathVariable Long itemSalesId) {
    	SalesItemDto salesItem = salesItemService.getSalesItemById(itemSalesId);
        if (salesItem != null) {
            return ResponseEntity.ok(salesItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 상품 판매 수정
    @PutMapping("/products/{itemSalesId}") 
    public ResponseEntity<String> updateSalesItem(
            @PathVariable Long itemSalesId, 
            @RequestBody SalesItemDto salesItem) {
        
        salesItem.setItemSalesId(itemSalesId); 
        
        try {
            int result = salesItemService.updateSalesItem(salesItem); 
            if (result > 0) {
                return ResponseEntity.ok("판매 내역이 성공적으로 수정되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("수정할 판매 내역을 찾을 수 없습니다.");
            }
        } catch (RuntimeException e) { // RuntimeException으로 변경하여 재고 부족 예외를 명확히 처리
            logger.error("판매 내역 수정 중 오류 발생: itemSalesId={}", itemSalesId, e); // 상세 로깅 추가
            if (e.getMessage() != null && e.getMessage().contains("재고가 부족")) {
                // 예외 메시지에서 최대 수량 추출
                String errorMessage = e.getMessage();
                int availableQty = 0;
                try {             
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("최대 수량: (\\d+)");
                    java.util.regex.Matcher matcher = pattern.matcher(errorMessage);
                    if (matcher.find()) {
                        availableQty = Integer.parseInt(matcher.group(1));
                    }
                } catch (NumberFormatException ex) {
                    logger.warn("재고 부족 메시지에서 최대 수량 추출 실패: {}", errorMessage);
                }

                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    String.format("입력 가능한 최대 수량은 %d개입니다.", availableQty)
                );
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("판매 내역 수정 중 서버 오류가 발생했습니다.");
        } catch (Exception e) {
            logger.error("판매 내역 수정 중 예상치 못한 오류 발생: itemSalesId={}", itemSalesId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("판매 내역 수정 중 서버 오류가 발생했습니다.");
        }
    }

    // 상품 판매 삭제
    @DeleteMapping("/products/{itemSalesId}") 
    public ResponseEntity<String> deleteSalesItem(@PathVariable Long itemSalesId) {
        try {
            int result = salesItemService.deleteSalesItem(itemSalesId);
            if (result > 0) {
                return ResponseEntity.ok("판매 내역이 성공적으로 삭제(취소)되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("삭제할 판매 내역을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("판매 내역 삭제 중 서버 오류가 발생했습니다.");
        }
    }

}