package com.example.gymerp.dto;

import jakarta.validation.constraints.NotBlank;

/*
 * 로그인 요청 (POST /v1/emp/login)
 * empEmail 에 "admin" 입력 시 관리자 계정으로 로그인 (CustomUserDetailsService 참고)
 */
public record LoginRequest(
		@NotBlank(message = "이메일을 입력해 주세요.") String empEmail,
		@NotBlank(message = "비밀번호를 입력해 주세요.") String password) {
}
