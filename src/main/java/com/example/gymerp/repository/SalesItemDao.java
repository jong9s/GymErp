package com.example.gymerp.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; 

import com.example.gymerp.dto.SalesItemDto;

@Mapper
public interface SalesItemDao {

    // 전체 판매 내역 조회 (페이징/필터링 파라미터 포함)
    List<SalesItemDto> selectAllSalesItems(Map<String, Object> params);

    // 전체 개수 조회 (페이징을 위해 필수)
    int selectSalesItemCount(Map<String, Object> params);

    // 단일 판매 내역 조회
    SalesItemDto selectSalesItemById(@Param("itemSalesId") Long itemSalesId);

    // ========== [재고/수정 관련 추가 메서드] ==========

    // 판매 내역 수정 및 삭제 시, 기존 수량(oldQuantity)과 상품 정보(productId, codeBId)를 조회합니다.
    Map<String, Object> selectSalesItemForAdjustment(@Param("itemSalesId") Long itemSalesId);


    // 재고 환원 (입고) 내역을 Purchase 테이블에 기록합니다. (판매 취소/수량 감소 시 사용)

    void insertPurchaseForRefund(Map<String, Object> params);

    // 판매 내역 등록
    int insertSalesItem(SalesItemDto salesItem);

    // 판매 내역 수정
    int updateSalesItem(SalesItemDto salesItem);


	// 상품 판매 내역을 소프트 삭제(status='DELETED') 처리합니다.
	int deleteSalesItem(Long itemSalesId);

    // 상품 매출 통계 조회
    List<Map<String, Object>> selectItemSalesAnalytics(Map<String, Object> params);

    
}