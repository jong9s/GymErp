package com.example.gymerp.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.gymerp.security.CustomUserDetails;
import com.example.gymerp.service.EmpService;
import com.example.gymerp.service.FileStorageService;
import com.example.gymerp.support.ControllerSliceTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 로그인 API 슬라이스 테스트 (POST /v1/emp/login)
 */
@WebMvcTest(
    controllers = EmpController.class,
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
class EmpLoginControllerTest extends ControllerSliceTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean EmpService empService;
    @MockitoBean FileStorageService fileStorageService;
    @MockitoBean AuthenticationManager authenticationManager;

    @Test
    @DisplayName("로그인 성공 - 사용자 정보를 반환하고 세션에 인증 정보를 저장한다 (sessionId 는 응답에 노출하지 않음)")
    void login_success() throws Exception {
        CustomUserDetails user = new CustomUserDetails(1, "관리자", "admin@gym.local", "hash", "ADMIN");
        when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        mockMvc.perform(post("/v1/emp/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("empEmail", "admin", "password", "pw"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.empNum").value(1))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.sessionId").doesNotExist())
            .andExpect(request().sessionAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, notNullValue()));
    }

    @Test
    @DisplayName("로그인 실패 - 아이디/비밀번호가 틀리면 401 을 반환한다")
    void login_badCredentials() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/v1/emp/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("empEmail", "x@test.com", "password", "wrong"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("로그인 요청 검증 - 이메일/비밀번호가 비어 있으면 400 을 반환한다")
    void login_blank() throws Exception {
        mockMvc.perform(post("/v1/emp/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("empEmail", "", "password", ""))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.empEmail").exists())
            .andExpect(jsonPath("$.errors.password").exists());
    }
}
