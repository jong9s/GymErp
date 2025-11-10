package com.example.gymerp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymerp.dto.SalesItemDto;
import com.example.gymerp.repository.ProductDao;
import com.example.gymerp.repository.SalesItemDao;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.gymerp.dto.ProductDto;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesItemServiceImpl implements SalesItemService {

	private static final Logger logger = LoggerFactory.getLogger(SalesItemServiceImpl.class);

	private final SalesItemDao salesItemDao;

	private final ProductDao productDao;

	private final StockService stockService;

	// 상품 전체 목록 조회
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getAllSalesItems(String startDate, String endDate, String productNameKeyword,
			Integer empNum, int page, int size) {

		Map<String, Object> params = new HashMap<>();
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		if (productNameKeyword != null && !productNameKeyword.trim().isEmpty()) {
			params.put("productNameKeyword", productNameKeyword);
		}
		params.put("empNum", empNum);
		int startRow = (page - 1) * size + 1;
		int endRow = page * size;
		params.put("startRow", startRow);
		params.put("endRow", endRow);

		List<SalesItemDto> salesItems = salesItemDao.selectAllSalesItems(params);
		int totalCount = salesItemDao.selectSalesItemCount(params);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("list", salesItems);
		resultMap.put("totalCount", totalCount);
		resultMap.put("currentPage", page);
		resultMap.put("pageSize", size);

		return resultMap;
	}

	@Override
	public SalesItemDto getSalesItemById(Long itemSalesId) {
		return salesItemDao.selectSalesItemById(itemSalesId);
	}

	// 상품 판매 내역을 등록합니다.
	@Override
	public int addSalesItem(SalesItemDto salesItem) {

		// 1. 판매 가능 여부 확인 (StockService를 통해 재고 체크)
		if (!stockService.isStockSufficient(salesItem.getProductId(), salesItem.getQuantity())) {
			throw new RuntimeException("판매 수량에 비해 상품 재고가 부족합니다. (상품 ID: " + salesItem.getProductId() + ")");
		}

		// 2. 상품 정보 조회 및 DTO 필드 설정
		ProductDto product = productDao.getByNum(salesItem.getProductId());
		if (product == null) {
			throw new RuntimeException("상품 정보를 찾을 수 없습니다. productId: " + salesItem.getProductId());
		}
		salesItem.setProductName(product.getName());
		salesItem.setProductType(product.getCodeBId());
		salesItem.setStatus("ACTIVE");

		if (salesItem.getUnitPrice() == null) {
			salesItem.setUnitPrice(product.getPrice());
		}

		LocalDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul")).toLocalDateTime();
		salesItem.setCreatedAt(now);
		salesItem.setUpdatedAt(now);
		BigDecimal quantityBd = BigDecimal.valueOf(salesItem.getQuantity());
		salesItem.setTotalAmount(salesItem.getUnitPrice().multiply(quantityBd).setScale(0, RoundingMode.DOWN));

		logger.debug("SalesItemDto before insert: {}", salesItem); // Debug log

		int result = salesItemDao.insertSalesItem(salesItem);

		return result;
	}

	// 상품 판매 내역 수정
	@Override
	public int updateSalesItem(SalesItemDto salesItem) {
		Long itemSalesId = salesItem.getItemSalesId();

		salesItem.setUpdatedAt(java.time.LocalDateTime.now());

		// 🚨 총액 계산
		BigDecimal quantityBd = BigDecimal.valueOf(salesItem.getQuantity());
		salesItem.setTotalAmount(salesItem.getUnitPrice().multiply(quantityBd).setScale(0, RoundingMode.DOWN));

		return salesItemDao.updateSalesItem(salesItem);
	}

	// 상품 판매 내역 삭제 (소프트 삭제 구현)
	@Override
	public int deleteSalesItem(Long itemSalesId) {

		return salesItemDao.deleteSalesItem(itemSalesId);
	}

	// 상품 매출 통계 조회
	@Override
	public List<Map<String, Object>> getItemSalesAnalytics(String startDate, String endDate, List<Integer> itemIds,
			Integer memNum, Integer empNum) {

		Map<String, Object> params = new HashMap<>();
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("itemIds", itemIds);
		params.put("memNum", memNum);
		params.put("empNum", empNum);

		return salesItemDao.selectItemSalesAnalytics(params);
	}

}