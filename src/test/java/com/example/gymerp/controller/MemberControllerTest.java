package com.example.gymerp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.dto.PageResponse;
import com.example.gymerp.service.FileStorageService;
import com.example.gymerp.service.MemberService;
import com.example.gymerp.support.ControllerSliceTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MemberController 슬라이스 테스트
 * - 입력값 검증(@Valid) 실패 시 400 + 필드별 사유
 * - 서비스 예외가 GlobalExceptionHandler 를 통해 JSON 으로 변환되는지 확인
 */
@WebMvcTest(
    controllers = MemberController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class),
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Mapper.class)
    },
    excludeAutoConfiguration = {
        MybatisAutoConfiguration.class,
        MybatisLanguageDriverAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest extends ControllerSliceTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean MemberService memberService;
    @MockitoBean FileStorageService fileStorageService;

    private Map<String, Object> validMember() {
        return Map.of(
            "memName", "홍길동",
            "memGender", "남",
            "memBirthday", "1995-03-01",
            "memPhone", "010-1234-5678",
            "memEmail", "hong@test.com"
        );
    }

    @Test
    @DisplayName("GET /v1/member - 기본값 page=1,size=20 으로 페이지 조회하고 페이지 정보를 함께 반환한다")
    void list_paged() throws Exception {
        MemberDto m = new MemberDto();
        m.setMemNum(1);
        m.setMemName("홍길동");
        when(memberService.getPageWithStats("ALL", 1, 20)).thenReturn(PageResponse.of(List.of(m), 1, 20, 41));

        mockMvc.perform(get("/v1/member"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list[0].memName").value("홍길동"))
            .andExpect(jsonPath("$.totalCount").value(41))
            .andExpect(jsonPath("$.totalPage").value(3));
    }

    @Test
    @DisplayName("GET /v1/member - size 가 100 을 넘으면 400 (한 번에 전체 조회 방지)")
    void list_sizeTooLarge() throws Exception {
        mockMvc.perform(get("/v1/member").param("size", "100000"))
            .andExpect(status().isBadRequest());

        verify(memberService, never()).getPageWithStats(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("POST /v1/member - 정상 입력이면 생성된 회원번호를 반환한다")
    void create_success() throws Exception {
        doAnswer(inv -> {
            inv.<MemberDto>getArgument(0).setMemNum(101);
            return null;
        }).when(memberService).createMember(any(MemberDto.class));

        mockMvc.perform(post("/v1/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validMember())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memNum").value(101));
    }

    @Test
    @DisplayName("POST /v1/member - 필수값 누락/형식 오류면 400과 필드별 사유를 반환하고 저장하지 않는다")
    void create_validationFail() throws Exception {
        Map<String, Object> body = Map.of(
            "memName", "",
            "memGender", "남",
            "memBirthday", "1995-03-01",
            "memPhone", "02-123",
            "memEmail", "not-an-email"
        );

        mockMvc.perform(post("/v1/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.memName").exists())
            .andExpect(jsonPath("$.errors.memPhone").exists())
            .andExpect(jsonPath("$.errors.memEmail").exists());

        verify(memberService, never()).createMember(any());
    }

    @Test
    @DisplayName("PUT /v1/member/{memNum} - 경로의 회원번호로 수정하고 204를 반환한다")
    void update_success() throws Exception {
        mockMvc.perform(put("/v1/member/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validMember())))
            .andExpect(status().isNoContent());

        verify(memberService).updateMember(argThat(dto -> dto.getMemNum() == 7));
    }

    @Test
    @DisplayName("POST /v1/member/upload/{memNum} - 저장된 파일명을 반환하고 회원 프로필을 갱신한다")
    void upload_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "me.png", "image/png", new byte[] { 1, 2, 3 });
        when(fileStorageService.storeImage(any(), eq("7"))).thenReturn("7_123.png");

        mockMvc.perform(multipart("/v1/member/upload/7").file(file))
            .andExpect(status().isOk())
            .andExpect(content().string("7_123.png"));

        verify(memberService).updateMemberProfile(7, "7_123.png");
    }

    @Test
    @DisplayName("POST /v1/member/upload/{memNum} - 허용되지 않은 파일이면 400 JSON 을 반환한다")
    void upload_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "run.exe", "application/octet-stream", new byte[] { 1 });
        when(fileStorageService.storeImage(any(), eq("7")))
            .thenThrow(new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다."));

        mockMvc.perform(multipart("/v1/member/upload/7").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이미지 파일만 업로드할 수 있습니다."));

        verify(memberService, never()).updateMemberProfile(anyInt(), any());
    }
}
