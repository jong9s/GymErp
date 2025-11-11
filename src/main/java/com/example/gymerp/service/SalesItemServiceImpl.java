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
import com.example.gymerp.dto.StockAdjustRequestDto;

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
			// 현재 유효한 재고 수량을 가져옵니다.
			int availableStock = stockService.getStockOne(salesItem.getProductId());
			// 수정 기능과 동일하게, 최대 수량을 포함한 상세한 오류 메시지를 생성합니다.
			throw new RuntimeException(String.format("재고가 부족합니다. 최대 입력 가능 수량은 %d개입니다.", availableStock));
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
	    public int updateSalesItem(SalesItemDto updatedSalesItem) {
	        try {
	            // 올바른 총액 계산을 위해 기존 상품의 단가를 조회합니다.
	            SalesItemDto originalSalesItem = salesItemDao.selectSalesItemById(updatedSalesItem.getItemSalesId());
	            if (originalSalesItem == null) {
	                logger.warn("수정할 판매 내역을 찾을 수 없습니다. ID: {}", updatedSalesItem.getItemSalesId());
	                throw new RuntimeException("수정할 판매 내역을 찾을 수 없습니다. ID: " + updatedSalesItem.getItemSalesId());
	            }
	
	                        // --- 재고 수량 확인 로직 추가 시작 ---
	                        int oldQuantity = originalSalesItem.getQuantity();
	                        int newQuantity = updatedSalesItem.getQuantity();
	                        int productId = updatedSalesItem.getProductId();
	            
	                        if (newQuantity > oldQuantity) { // 수량이 증가하는 경우 (재고 감소)
	                            int quantityToDecrease = newQuantity - oldQuantity;
	                            if (!stockService.isStockSufficient(productId, quantityToDecrease)) {
	                                int available = stockService.getStockOne(productId);
	                                throw new RuntimeException(String.format("판매 수량에 비해 상품 재고가 부족합니다. (상품 ID: %d, 입력 가능한 최대 수량: %d)", productId, available));
	                            }
	                        }
	                        // --- 재고 수량 확인 로직 추가 끝 ---
	            
	                        // 판매 내역 업데이트
	                        updatedSalesItem.setUpdatedAt(LocalDateTime.now());
	            
	                        // 총액 계산
	                        BigDecimal quantityBd = BigDecimal.valueOf(newQuantity); // Use newQuantity for total amount calculation
	                        BigDecimal unitPrice = originalSalesItem.getUnitPrice(); // 단가는 기존 판매 내역의 값을 사용
	                        updatedSalesItem.setUnitPrice(unitPrice);
	                        updatedSalesItem.setTotalAmount(unitPrice.multiply(quantityBd).setScale(0, RoundingMode.DOWN));	
	            // DAO를 통해 판매 내역만 업데이트합니다. 재고는 DB 쿼리에서 자동으로 계산됩니다.
	            logger.debug("Updating SalesItem: {}", updatedSalesItem); // Debug log
	            return salesItemDao.updateSalesItem(updatedSalesItem);
	                                } catch (Exception e) {
	                                    logger.error("SalesItemService.updateSalesItem 중 오류 발생: updatedSalesItem={}", updatedSalesItem, e);
	                                    throw e; // 예외를 다시 던져 컨트롤러에서 처리하도록 합니다.
	                                }	    }
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