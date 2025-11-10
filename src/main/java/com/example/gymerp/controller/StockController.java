package com.example.gymerp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gymerp.dto.CurrentStockDto;
import com.example.gymerp.dto.PaginatedResponse;
import com.example.gymerp.dto.PurchaseDto;
import com.example.gymerp.dto.StockAdjustRequestDto;
import com.example.gymerp.dto.StockAdjustmentDto;
import com.example.gymerp.service.StockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;


@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
@Validated
public class StockController {
	
	private final StockService stockService;
	
	//개별 상품 재고 조회
	@GetMapping("/stock/{productId}")
	public int getProductStock(@PathVariable int productId) {
		
		return stockService.getStockOne(productId);
	}
	
	// 2-3. 상품 재고 조회. 상품테이블에서 구분 코드와 상품명 가져오기, 입출고 테이블에서 입고수량-출고수량 표시
//	@GetMapping("/stock")
//	public List<CurrentStockDto> getProductStockList(
//	    @RequestParam(defaultValue = "1") 
//	    int page,
//	    @RequestParam(defaultValue = "20") 
//	    int size,
//	    @RequestParam(required = false) String 
//	    keyword
//	) {
//	    return stockService.getProductStockList(page, size, keyword);
//	}
	
	// 상품 입고 내역 조회
	@GetMapping("/stock/{productId}/inbound")
	public PaginatedResponse<PurchaseDto> getProductInboundDetail(
	        @PathVariable int productId,
	        @RequestParam(defaultValue = "1") int inboundPage,
	        @RequestParam(defaultValue = "20") int size,
	        @RequestParam(required = false) String startDate,
	        @RequestParam(required = false) String endDate) {
	    return stockService.getProductInboundDetail(productId, inboundPage, size, startDate, endDate);
	}
	
	// 상품 출고 내역 조회
	@GetMapping("/stock/{productId}/outbound")
	public PaginatedResponse<StockAdjustmentDto> getProductOutboundDetail(
	        @PathVariable int productId,
	        @RequestParam(defaultValue = "1") int outboundPage,
	        @RequestParam(defaultValue = "20") int size,
	        @RequestParam(required = false) String startDate,
	        @RequestParam(required = false) String endDate) {
	    return stockService.getProductOutboundDetail(productId, outboundPage, size, startDate, endDate);
	}
	
	// 상품의 재고를 추가 및 차감 
	@PostMapping("/stock/{productId}/adjust")
	public ResponseEntity<Map<String, Object>> adjustProduct(
	        @PathVariable int productId,
	        @RequestBody @Valid StockAdjustRequestDto request
	) {
		stockService.adjustProduct(productId, request);
		Map<String, Object> responseBody = Map.of("message", "재고 조정에 성공했습니다.");
		return ResponseEntity.ok(responseBody);
	}
}
