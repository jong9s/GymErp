package com.example.gymerp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SalesItemDto {

    private Long itemSalesId;        // 상품 판매 ID (PK)         
    private int productId;          // 실물 상품 고유번호 (FK)     
    private int empNum;             // 직원 고유번호 (FK)          

    private String productName;      // 상품명                        
    private int quantity;        // 수량                       

    private BigDecimal unitPrice;    // 단가                         
    private BigDecimal totalAmount;  // 총액                  

    private String productType;      // 구분                         
    private String status;           // 상태 (기본값 'ACTIVE')       

    private LocalDateTime createdAt; // 등록일                   
    private LocalDateTime updatedAt; // 수정일
    
    private String empEmail;
    private String empName;
    
}
