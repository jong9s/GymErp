package com.example.gymerp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.gymerp.dto.ProductDto;
import com.example.gymerp.dto.ProductListResponse;
import com.example.gymerp.config.FileStorageProperties;
import com.example.gymerp.dto.StockAdjustRequestDto;
import com.example.gymerp.repository.ProductDao;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private StockService stockService;

    @Mock
    private FileStorageProperties fileStorageProperties;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("DAO를 호출하여 ProductDto를 반환한다")
    void getDetail_returnsProductDto() {
        // Given
        int productId = 1;
        ProductDto mockProduct = ProductDto.builder().productId(productId).name("테스트 상품").build();
        //만약(when) productDao라는 가짜 객체의 getByNum(productId) 메소드가 productId라는 값으로 호출되면," 
        //"그때(then) 실제 DB에 가지 말고, 우리가 미리 만들어둔 mockProduct라는 가짜 객체를 반환해라(Return)
        when(productDao.getByNum(productId)).thenReturn(mockProduct);

        // When
        ProductDto result = productService.getDetail(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("테스트 상품");
        verify(productDao).getByNum(productId); // productDao라는 가짜(Mock) 객체가 getByNum(productId) 메소드로 정확히 1번 호출되었는지 검증
        verifyNoMoreInteractions(productDao); // getByNum 호출 이외에, productDao 객체의 다른 어떤 메소드도 (예: insert, update 등) 추가로 호출되지 않았는지 검증
        verifyNoInteractions(stockService); // stockService라는 가짜(Mock) 객체는 아예 한 번도 호출되지 않았는지 (어떠한 상호작용(Interaction)도 없었는지) 검증
    }

    @Test
    @DisplayName("DAO를 호출하여 상품 상태를 변경한다")
    void updateProductStatus_callsDao() {
        // Given
        int productId = 1;
        boolean isActive = false;
        // 만약(when) productDao의 updateProductStatus 메소드가 ProductDto 타입의 어떤 객체든(any()) 받아서 호출되면," 
        //"그때(then) 실제 DB를 업데이트하지 말고, 그냥 '1'이라는 숫자(성공, 1개 행이 반영됨)를 반환해라(Return)
        when(productDao.updateProductStatus(any(ProductDto.class))).thenReturn(1); 
        

        // When
        productService.updateProductStatus(productId, isActive);

        // Then
        ArgumentCaptor<ProductDto> captor = ArgumentCaptor.forClass(ProductDto.class); // 테스트 대상(Service)을 실행한 후에, 가짜 부품(DAO)으로 무엇이 전달되었는지(인자)를 붙잡아서 검증(Verification)
        verify(productDao).updateProductStatus(captor.capture());
        ProductDto capturedDto = captor.getValue();
        assertThat(capturedDto.getProductId()).isEqualTo(productId);
        assertThat(capturedDto.getIsActive()).isEqualTo(isActive);
    }

    @Test
    @DisplayName("업데이트 실패 시 예외를 던진다")
    void updateProductStatus_throwsExceptionOnFailure() {
        // Given
        int productId = 99; // 존재하지 않는 ID
        when(productDao.updateProductStatus(any(ProductDto.class))).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> productService.updateProductStatus(productId, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("상품 상태 업데이트 실패");
    }

    @Nested
    @DisplayName("getProducts")
    class GetProducts {

        @Test
        @DisplayName("페이지네이션 정보와 함께 상품 목록을 반환한다")
        void returnsPaginatedProductList() {
            // Given
            int pageNum = 2;
            ProductDto searchDto = new ProductDto();
            ProductDto product1 = ProductDto.builder().productId(1).name("상품1").build();
            ProductDto product2 = ProductDto.builder().productId(2).name("상품2").build();
            List<ProductDto> productList = List.of(product1, product2);

            when(productDao.getCount(searchDto)).thenReturn(25); // 총 25개 상품
            when(productDao.selectPage(any(ProductDto.class))).thenReturn(productList);
            when(stockService.getStockOne(1)).thenReturn(10);
            when(stockService.getStockOne(2)).thenReturn(20);

            // When
            ProductListResponse response = productService.getProducts(pageNum, searchDto, "name", "ASC");

            // Then
            // 페이지네이션 검증 (총 25개, 한 페이지에 10개 -> 총 3페이지)
            assertThat(response.getTotalRow()).isEqualTo(25);
            assertThat(response.getTotalPageCount()).isEqualTo(3);
            assertThat(response.getPageNum()).isEqualTo(pageNum);
            assertThat(response.getStartPageNum()).isEqualTo(1);
            assertThat(response.getEndPageNum()).isEqualTo(3);

            // DTO 전달 검증
            ArgumentCaptor<ProductDto> captor = ArgumentCaptor.forClass(ProductDto.class);
            verify(productDao).selectPage(captor.capture());
            ProductDto capturedDto = captor.getValue();
            assertThat(capturedDto.getStartRowNum()).isEqualTo(11); // (2-1)*10 + 1
            assertThat(capturedDto.getEndRowNum()).isEqualTo(20);   // 2*10
            assertThat(capturedDto.getSortBy()).isEqualTo("name");
            assertThat(capturedDto.getDirection()).isEqualTo("ASC");

            // 재고 수량 검증
            assertThat(response.getList()).hasSize(2);
            assertThat(response.getList().get(0).getQuantity()).isEqualTo(10);
            assertThat(response.getList().get(1).getQuantity()).isEqualTo(20);
            verify(stockService, times(2)).getStockOne(anyInt());
        }

        @Test
        @DisplayName("상품이 없을 경우 빈 목록을 반환한다")
        void returnsEmptyListWhenNoProducts() {
            // Given
            when(productDao.getCount(any(ProductDto.class))).thenReturn(0);
            when(productDao.selectPage(any(ProductDto.class))).thenReturn(Collections.emptyList());

            // When
            ProductListResponse response = productService.getProducts(1, new ProductDto(), null, null);

            // Then
            assertThat(response.getTotalRow()).isZero();
            assertThat(response.getTotalPageCount()).isZero();
            assertThat(response.getList()).isEmpty();
            verifyNoInteractions(stockService);
        }
    }

    @Nested
    @DisplayName("save")
    class SaveProduct {

        @Test
        @DisplayName("이미지 파일 없이 상품 정보를 저장한다")
        void savesProductWithoutImage() {
            // Given
            ProductDto productDto = ProductDto.builder().name("새 상품").quantity(0).build();
            StockAdjustRequestDto stockDto = new StockAdjustRequestDto();

            // When
            productService.save(productDto, stockDto);

            // Then
            verify(productDao).insert(productDto);
            assertThat(productDto.getProfileImage()).isNull();
            verifyNoInteractions(stockService); // quantity가 0이므로 재고 조정 호출 안 함
        }

        @Test
        @DisplayName("이미지 파일과 함께 상품 정보를 저장한다")
        void savesProductWithImage() throws IOException {
            // Given
            MockMultipartFile imageFile = new MockMultipartFile("profileFile", "hello.png", "image/png", "some-image".getBytes());
            ProductDto productDto = ProductDto.builder().name("이미지 상품").profileFile(imageFile).quantity(0).build();
            StockAdjustRequestDto stockDto = new StockAdjustRequestDto();

            // When
            productService.save(productDto, stockDto);

            // Then
            verify(productDao).insert(productDto);
            assertThat(productDto.getProfileImage()).isNotNull();
            assertThat(productDto.getProfileImage()).endsWith(".png");
        }

        @Test
        @DisplayName("재고 수량이 0보다 크면 재고 조정을 호출한다")
        void callsStockAdjustmentWhenQuantityIsPositive() {
            // Given
            ProductDto productDto = ProductDto.builder().productId(99).name("재고 있는 상품").quantity(5).build();
            StockAdjustRequestDto stockDto = new StockAdjustRequestDto();

            // When
            productService.save(productDto, stockDto);

            // Then
            verify(productDao).insert(productDto);
            verify(stockService).adjustProduct(eq(99), any(StockAdjustRequestDto.class));
        }
        
        @Test
        @DisplayName("재고 수량이 0이면 재고 조정을 호출하지 않는다")
        void callsStockAdjustmentWhenQuantityIsZero() {
            // Given
            ProductDto productDto = ProductDto.builder().quantity(0).build(); // 수량이 0
            StockAdjustRequestDto stockDto = new StockAdjustRequestDto();

            // When
            productService.save(productDto, stockDto);

            // Then
            verify(productDao).insert(productDto); // insert는 호출됨
            
            // stockService는 '절대(never)' 호출되지 않음
            verify(stockService, never()).adjustProduct(anyInt(), any()); 
        }
    }
    
    @Nested
    @DisplayName("modifyProduct")
    class ModifyProduct {

        @Test
        @DisplayName("이미지 변경 없이 상품 정보를 수정한다")
        void modifiesProductWithoutImage() {
            // Given
            ProductDto productDto = ProductDto.builder().productId(1).name("수정된 상품").build();
            when(productDao.update(productDto)).thenReturn(1);

            // When
            productService.modifyProduct(productDto);

            // Then
            verify(productDao).update(productDto);
            assertThat(productDto.getProfileImage()).isNull();
        }

        @Test
        @DisplayName("새 이미지로 상품 정보를 수정한다")
        void modifiesProductWithNewImage() {
            // Given
            MockMultipartFile imageFile = new MockMultipartFile("profileFile", "new.jpg", "image/jpeg", "new-image".getBytes());
            ProductDto productDto = ProductDto.builder().productId(1).name("수정된 상품").profileFile(imageFile).build();
            when(productDao.update(productDto)).thenReturn(1);

            // When
            productService.modifyProduct(productDto);

            // Then
            verify(productDao).update(productDto);
            assertThat(productDto.getProfileImage()).isNotNull();
            assertThat(productDto.getProfileImage()).endsWith(".jpg");
        }

        @Test
        @DisplayName("수정 대상이 없으면 예외를 던진다")
        void throwsExceptionWhenUpdateFails() {
            // Given
            ProductDto productDto = ProductDto.builder().productId(999).name("없는 상품").build();
            when(productDao.update(productDto)).thenReturn(0);

            // When & Then
            assertThatThrownBy(() -> productService.modifyProduct(productDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("상품 수정 실패!");
        }
    }
}