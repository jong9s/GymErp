package com.example.gymerp.controller;

import com.example.gymerp.dto.SalesItemDto;
import com.example.gymerp.service.SalesItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalesItemController.class)
class SalesItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SalesItemService salesItemService;

    @Test
    @DisplayName("POST /v1/sales/products - 판매 내역 등록 성공")
    void addSalesItem_Success() throws Exception {
        // Given (준비)
        SalesItemDto requestDto = SalesItemDto.builder()
                .productId(1)
                .empNum(1)
                .quantity(2)
                .build();

        // Mock Service: addSalesItem 메서드가 호출되면 생성된 ID(예: 100L)를 반환하도록 설정
        when(salesItemService.addSalesItem(any(SalesItemDto.class))).thenReturn(100);

        // When & Then (실행 및 검증)
        mockMvc.perform(post("/v1/sales/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // 201 Created 상태 코드 확인
                .andExpect(jsonPath("$.message").value("판매 내역이 성공적으로 등록되었습니다.")) // 응답 메시지 확인
                .andExpect(jsonPath("$.id").value(100)); // 생성된 ID 확인
    }

    // TODO: 판매 등록 실패 케이스 (예: 재고 부족으로 서비스에서 예외 발생)
    // TODO: 판매 등록 실패 케이스 (예: 유효성 검사 실패 - @Valid)
}
