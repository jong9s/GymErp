package com.example.gymerp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymerp.dto.CurrentStockDto;
import com.example.gymerp.dto.PaginatedResponse;
import com.example.gymerp.dto.ProductDto;
import com.example.gymerp.dto.PurchaseDto;
import com.example.gymerp.dto.StockAdjustRequestDto;
import com.example.gymerp.dto.StockAdjustmentDto;
import com.example.gymerp.repository.ProductDao;
import com.example.gymerp.repository.StockDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
	
	private final StockDao stockDao;
	private final ProductDao productDao;
	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;

	// 1-1. 가용 재고 조회
	@Override
	public int getStockOne(int productId) {
		
		return stockDao.getAvailableQty(productId);
	}
	
	// 1-2. 판매 가능 여부 리턴
	@Override
	public Boolean isStockSufficient(int productId, int quantity) {
		return stockDao.getAvailableQty(productId) >= quantity;
	}	

    // 2-1. 상품 하나의 입고(인바운드) 내역 조회
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<PurchaseDto> getProductInboundDetail(int productId, int inboundPage, int size, String startDate, String endDate) {
        if (productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 productId 입니다.");
        }
        int normalizedPage = normalizePage(inboundPage);
        int normalizedSize = normalizeSize(size);
        int offset = (normalizedPage - 1) * normalizedSize;
        
        // DAO에 전달할 파라미터 맵 생성
        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        params.put("offset", offset);
        params.put("size", normalizedSize);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        
        // 1. 전체 아이템 개수 조회
        int totalItemCount = stockDao.getPurchaseListCount(params);
        
        // 2. 전체 페이지 수 계산 (0개일 경우 1페이지로 처리)
        int totalPageCount = (totalItemCount == 0) ? 1 : (int) Math.ceil((double) totalItemCount / normalizedSize);

        // 3. 현재 페이지 목록 데이터 조회
        List<PurchaseDto> list = stockDao.getPurchaseList(params);
        
        return PaginatedResponse.<PurchaseDto>builder()
        		.list(list)
        		.pageNum(normalizedPage)
        		.totalPageCount(totalPageCount)
        		.build();
    }

    // 2-2. 상품 하나의 출고 + 판매 내역 조회
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<StockAdjustmentDto> getProductOutboundDetail(int productId, int outboundPage, int size, String startDate, String endDate) {
        if (productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 productId 입니다.");
        }
        int normalizedPage = normalizePage(outboundPage);
        int normalizedSize = normalizeSize(size);
        int offset = (normalizedPage - 1) * normalizedSize;
        
        // DAO에 전달할 파라미터 맵 생성
        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        params.put("offset", offset);
        params.put("size", normalizedSize);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        
        // 1. 전체 아이템 개수 조회
        int totalItemCount = stockDao.getAdjustStockAndSalesListCount(params);
        
        // 2. 전체 페이지 수 계산 (0개일 경우 1페이지로 처리)
        int totalPageCount = (totalItemCount == 0) ? 1 : (int) Math.ceil((double) totalItemCount / normalizedSize);

        // 3. 현재 페이지 목록 데이터 조회
        List<StockAdjustmentDto> list = stockDao.getAdjustStockAndSalesList(params);
        
        return PaginatedResponse.<StockAdjustmentDto>builder()
        		.list(list)
        		.pageNum(normalizedPage)
        		.totalPageCount(totalPageCount)
        		.build();
    }
    
    
    // 2-3. 현재 재고내역 리스트 페이지 (현재 재고 현황)
    @Override
    @Transactional(readOnly = true)
    public List<CurrentStockDto> getProductStockList(int page, int size, String keyword) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        int offset = (normalizedPage - 1) * normalizedSize;
        return stockDao.getCurrentStockListPaged(offset, normalizedSize, keyword);
    }

	// 3. adjustProduct
	/**
	 * 상품 재고 조정 흐름
	 *  1. 요청 파라미터의 기본 유효성 확인 (수량·액션)
	 *  2. 상품 메타 조회로 존재 여부 및 codeBId 확보
	 *  3. 차감 요청인 경우 현재 가용 재고와 비교해 부족 시 즉시 예외 처리
	 *  4. 최종적으로 입고/출고 각 전용 INSERT 쿼리를 호출하고 1행 반영 여부를 확인
	 */
	@Override
	@Transactional
	public void adjustProduct(int productId, StockAdjustRequestDto request) {
		// 1) 요청 파라미터 기초 검증
	    if (request.getQuantity() <= 0) {
	        throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
	    }
	    
	   ProductDto productDto = productDao.getByNum(productId);
	    if (productDto == null) {
	        throw new IllegalArgumentException("유효하지 않은 상품입니다: productId=" + productId);
	    }

	    String codeBId = productDto.getCodeBId();
	    String action = request.getAction();
	    if (action == null || action.isBlank()) { //재고 비었는지 확인 
	        throw new IllegalArgumentException("action 값은 필수입니다."); // 비었으면 예외처리 
	    }

		    if ("SUBTRACT".equalsIgnoreCase(action)) { // 재고 있으면 차감 가능한지 인지 확인 
		    	// 2) 차감 시에는 현재 가용 재고를 다시 조회해 충분한지 확인
		    	int availableQty = stockDao.getAvailableQty(productId);
		    	
		        boolean hasEnoughStock = availableQty >= request.getQuantity();
		        if (!hasEnoughStock) {
		            throw new IllegalArgumentException(
		                    "재고가 부족합니다. 요청 수량=" + request.getQuantity() + ", 가용 재고=" + availableQty);
		        }
		    }// 재고가 0이 아님 + 차감할만큼 재고가 있음.

	    // 3) ADD / SUBTRACT에 따라 각각 Purchase, StockAdjustment 테이블에 기록
	    if ("ADD".equalsIgnoreCase(action)) { // 대소문자 무시하고 문자열 비교 
	    	ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
			LocalDateTime ldt = zdt.toLocalDateTime();
	    	
	        PurchaseDto purchase = PurchaseDto.builder()
	        		.productId(productId)
	        		.codeBId(codeBId) 
	        		.quantity(request.getQuantity())
	        		.notes(request.getNotes())
	        		.createdAt(ldt) // 날짜 설정 추가
	        		.build();
	        int inserted = stockDao.insertPurchase(purchase); // call DaoImpl 3-1
	        if (inserted != 1) {
	            throw new IllegalStateException("입고 등록에 실패했습니다.");
	        }
	    } else if ("SUBTRACT".equalsIgnoreCase(action)) {
	    	ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
			LocalDateTime ldt = zdt.toLocalDateTime();
	    	
	        StockAdjustmentDto adjust = StockAdjustmentDto.builder()
	        		.productId(productId)
	        		.codeBId(codeBId) 
	        		.quantity(request.getQuantity())
	        		.notes(request.getNotes())
	        		.createdAt(ldt) // 날짜 설정 추가
	        		.build();
	        int inserted = stockDao.insertStockAdjustment(adjust); // call DaoImpl 3-2
	        if (inserted != 1) {
	            throw new IllegalStateException("출고(차감) 등록에 실패했습니다.");
	        }
	    } else {
	        throw new IllegalArgumentException("올바르지 않은 action 값입니다. 추가 또는 삭제를 선택해주세요.");
	    }
	}

    private int normalizePage(int page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

	

}
