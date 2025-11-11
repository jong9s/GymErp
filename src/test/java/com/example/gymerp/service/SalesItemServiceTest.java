package com.example.gymerp.service;

import com.example.gymerp.dto.ProductDto;
import com.example.gymerp.dto.SalesItemDto;
import com.example.gymerp.repository.ProductDao;
import com.example.gymerp.repository.SalesItemDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesItemServiceTest {

    @Mock
    private SalesItemDao salesItemDao;

    @Mock
    private ProductDao productDao;

    @Mock
    private StockService stockService;

    @InjectMocks
    private SalesItemServiceImpl salesItemService;

    @Test
    @DisplayName("판매 내역 등록 성공")
    void addSalesItem_Success() {
        // Given (준비)
        // 1. 테스트용 요청 DTO 생성
        SalesItemDto requestDto = SalesItemDto.builder()
                .productId(1)
                .quantity(2)
                .build();

        // 2. Mock 객체들이 반환할 가상 데이터 생성
        ProductDto product = ProductDto.builder()
                .productId(1)
                .name("테스트 보충제")
                .codeBId("TEST_TYPE")
                .price(BigDecimal.valueOf(50000))
                .build();

        // 3. Mock 객체의 행동 정의
        // - 재고는 충분하다고 가정
        when(stockService.isStockSufficient(anyInt(), anyInt())).thenReturn(true);
        // - 상품 ID로 조회 시, 위에서 만든 product 객체를 반환하도록 설정
        when(productDao.getByNum(anyInt())).thenReturn(product);
        // - DB에 삽입은 성공했다고 가정 (결과로 1을 반환)
        when(salesItemDao.insertSalesItem(any(SalesItemDto.class))).thenReturn(1);

        // When (실행)
        int result = salesItemService.addSalesItem(requestDto);

        // Then (검증)
        // 1. 결과 값이 1인지 확인
        assertThat(result).isEqualTo(1);

        // 2. DAO에 실제로 전달된 SalesItemDto를 캡처
        ArgumentCaptor<SalesItemDto> captor = ArgumentCaptor.forClass(SalesItemDto.class);
        verify(salesItemDao).insertSalesItem(captor.capture());
        SalesItemDto capturedDto = captor.getValue();

        // 3. 캡처된 DTO의 값들이 서비스 로직에 의해 올바르게 설정되었는지 확인
        assertThat(capturedDto.getProductName()).isEqualTo("테스트 보충제");
        assertThat(capturedDto.getStatus()).isEqualTo("ACTIVE");
        // 50000 (단가) * 2 (수량) = 100000
        assertThat(capturedDto.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));

        // 4. 각 Mock 객체의 메서드가 정확히 1번씩 호출되었는지 확인
        verify(stockService).isStockSufficient(1, 2);
        verify(productDao).getByNum(1);
        verify(salesItemDao).insertSalesItem(any(SalesItemDto.class));
    }

    // TODO: 재고 부족 시 예외 발생 테스트
    @Test
    @DisplayName("판매 내역 등록 실패: 재고 부족")
    void addSalesItem_InsufficientStock_ThrowsException() {
        // Given (준비)
        SalesItemDto requestDto = SalesItemDto.builder()
                .productId(1)
                .quantity(10) // 재고보다 많은 수량
                .build();

        // Mock 객체의 행동 정의: 재고가 부족하다고 가정
        when(stockService.isStockSufficient(anyInt(), anyInt())).thenReturn(false);

        // When & Then (실행 및 검증)
        RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> salesItemService.addSalesItem(requestDto),
                "판매 수량에 비해 상품 재고가 부족합니다. (상품 ID: 1)" // 예상되는 예외 메시지
        );

        // 예외 메시지 확인
        assertThat(thrown.getMessage()).contains("판매 수량에 비해 상품 재고가 부족합니다.");

        // stockService.isStockSufficient만 호출되고 다른 DAO 메서드는 호출되지 않았는지 확인
        verify(stockService).isStockSufficient(1, 10);
        verify(productDao, never()).getByNum(anyInt());
        verify(salesItemDao, never()).insertSalesItem(any(SalesItemDto.class));
    }

    // TODO: 상품 정보 없을 시 예외 발생 테스트
    @Test
    @DisplayName("판매 내역 등록 실패: 상품 정보 없음")
    void addSalesItem_ProductNotFound_ThrowsException() {
        // Given (준비)
        SalesItemDto requestDto = SalesItemDto.builder()
                .productId(999) // 존재하지 않는 상품 ID
                .quantity(1)
                .build();

        // Mock 객체의 행동 정의
        when(stockService.isStockSufficient(anyInt(), anyInt())).thenReturn(true); // 재고는 충분하다고 가정
        when(productDao.getByNum(anyInt())).thenReturn(null); // 상품을 찾을 수 없다고 가정

        // When & Then (실행 및 검증)
        RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> salesItemService.addSalesItem(requestDto),
                "상품 정보를 찾을 수 없습니다. productId: 999" // 예상되는 예외 메시지
        );

        // 예외 메시지 확인
        assertThat(thrown.getMessage()).contains("상품 정보를 찾을 수 없습니다.");

        // stockService.isStockSufficient와 productDao.getByNum은 호출되었지만, insertSalesItem은 호출되지 않았는지 확인
        verify(stockService).isStockSufficient(999, 1);
        verify(productDao).getByNum(999);
        verify(salesItemDao, never()).insertSalesItem(any(SalesItemDto.class));
    }
}
