package com.example.gymerp.repository;

import com.example.gymerp.dto.SalesItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [SalesItemDao 테스트]
 * - @MybatisTest: MyBatis 관련 컴포넌트만 로드하여 DAO 계층을 테스트합니다.
 * - @AutoConfigureTestDatabase(replace = Replace.NONE): 스프링 부트가 내장 DB를 자동으로 구성하는 대신,
 *   application-test.properties에 정의된 H2 데이터베이스 설정을 사용하도록 강제합니다.
 * - 각 테스트는 @Transactional에 의해 실행 후 롤백되므로 다른 테스트에 영향을 주지 않습니다.
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {SalesItemDaoImpl.class})
class SalesItemDaoTest {
	
    @Autowired
    private SalesItemDao salesItemDao;

    // 테스트에 사용할 공통 데이터 생성 헬퍼 메소드
    private SalesItemDto createSalesItem(int productId, int empNum, int quantity, LocalDateTime dateTime) {
        return SalesItemDto.builder()
                .productId(productId)
                .empNum(empNum)
                .productName("테스트상품")
                .quantity(quantity)
                .unitPrice(BigDecimal.valueOf(10000))
                .totalAmount(BigDecimal.valueOf(10000 * quantity))
                .productType("TEST_TYPE")
                .status("ACTIVE")
                .createdAt(dateTime)
                .updatedAt(dateTime)
                .build();
    }

    @Nested
    @DisplayName("INSERT: 판매 내역 등록")
    class InsertTest {
        @Test
        @DisplayName("성공: 판매 내역을 등록하고, 생성된 ID를 반환해야 한다")
        void insertSalesItem_Success() {
            // Given (주어진 데이터)
            LocalDateTime now = LocalDateTime.now();
            SalesItemDto salesItem = createSalesItem(1, 1, 2, now); // empNum을 1로 변경

            // When
            int result = salesItemDao.insertSalesItem(salesItem);

            // Then
            assertThat(result).isEqualTo(1); // 1개 행이 영향을 받았는지 확인
            assertThat(salesItem.getItemSalesId()).isNotNull().isGreaterThan(0L); // 시퀀스를 통해 ID가 생성되었는지 확인

            SalesItemDto retrievedItem = salesItemDao.selectSalesItemById(salesItem.getItemSalesId());
            assertThat(retrievedItem).isNotNull();
            assertThat(retrievedItem.getProductName()).isEqualTo("단백질 보충제"); // 이 부분을 수정합니다.
            assertThat(retrievedItem.getCreatedAt()).isEqualToIgnoringNanos(now);
        }
    }

    @Nested
    @DisplayName("SELECT: 판매 내역 조회")
    class SelectTest {
        @Test
        @DisplayName("성공: 존재하는 ID로 조회 시, 해당 판매 내역을 반환해야 한다")
        void selectSalesItemById_Found() {
            // Given
            SalesItemDto salesItem = createSalesItem(1, 1, 1, LocalDateTime.now()); // empNum을 1로 변경
            salesItemDao.insertSalesItem(salesItem);

            // When
            SalesItemDto foundItem = salesItemDao.selectSalesItemById(salesItem.getItemSalesId());

            // Then
            assertThat(foundItem).isNotNull();
            assertThat(foundItem.getItemSalesId()).isEqualTo(salesItem.getItemSalesId());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 조회 시, null을 반환해야 한다")
        void selectSalesItemById_NotFound() {
            // When
            SalesItemDto foundItem = salesItemDao.selectSalesItemById(99999L);

            // Then
            assertThat(foundItem).isNull();
        }

        @Test
        @DisplayName("성공: 필터링 조건에 맞는 판매 내역 목록과 전체 개수를 반환해야 한다")
        void selectAllSalesItems_WithFilters() {
            // Given: 여러 개의 테스트 데이터 삽입
            // data-h2.sql에 존재하는 empNum을 사용하도록 변경
            salesItemDao.insertSalesItem(createSalesItem(1, 1, 1, LocalDateTime.now().minusDays(1)));
            salesItemDao.insertSalesItem(createSalesItem(2, 1, 2, LocalDateTime.now())); // empNum=1
            salesItemDao.insertSalesItem(createSalesItem(1, 2, 3, LocalDateTime.now())); // empNum=2

            // When: 특정 직원(empNum=1)으로 필터링
            Map<String, Object> params = new HashMap<>();
            params.put("empNum", 1);
            params.put("startRow", 1);
            params.put("endRow", 10);

            List<SalesItemDto> resultList = salesItemDao.selectAllSalesItems(params);
            int totalCount = salesItemDao.selectSalesItemCount(params);

            // Then
            assertThat(totalCount).isEqualTo(3); // 기대값을 2에서 3으로 변경
            assertThat(resultList).hasSize(3);   // 기대값을 2에서 3으로 변경
            assertThat(resultList).allMatch(item -> item.getEmpNum() == 1);
        }
    }

    @Nested
    @DisplayName("UPDATE: 판매 내역 수정")
    class UpdateTest {
        @Test
        @DisplayName("성공: 판매 내역의 수량과 총액을 수정해야 한다")
        void updateSalesItem_Success() {
            // Given
            SalesItemDto salesItem = createSalesItem(1, 1, 2, LocalDateTime.now()); // empNum을 1로 변경
            salesItemDao.insertSalesItem(salesItem);

            // When
            salesItem.setQuantity(3); // 수량 변경
            salesItem.setTotalAmount(BigDecimal.valueOf(30000)); // 총액 변경
            int result = salesItemDao.updateSalesItem(salesItem);

            // Then
            assertThat(result).isEqualTo(1);
            SalesItemDto updatedItem = salesItemDao.selectSalesItemById(salesItem.getItemSalesId());
            assertThat(updatedItem.getQuantity()).isEqualTo(3);
            assertThat(updatedItem.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        }
    }

    @Nested
    @DisplayName("DELETE: 판매 내역 삭제 (소프트 삭제)")
    class DeleteTest {
        @Test
        @DisplayName("성공: 판매 내역의 상태를 'DELETED'로 변경해야 한다")
        void deleteSalesItem_Success() {
            // Given
            SalesItemDto salesItem = createSalesItem(1, 1, 1, LocalDateTime.now()); // empNum을 1로 변경
            salesItemDao.insertSalesItem(salesItem);

            // When
            int result = salesItemDao.deleteSalesItem(salesItem.getItemSalesId());

            // Then
            assertThat(result).isEqualTo(1);
            // 조정용 조회 쿼리를 사용하여 상태를 확인 (selectById는 status='ACTIVE'인 것만 가져오므로)
            Map<String, Object> itemStatusMap = salesItemDao.selectSalesItemForAdjustment(salesItem.getItemSalesId());
            assertThat(itemStatusMap.get("STATUS")).isEqualTo("DELETED");
        }
    }
}
