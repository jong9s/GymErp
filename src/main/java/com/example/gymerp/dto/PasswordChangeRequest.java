package com.example.gymerp.dto;

import jakarta.validation.constraints.NotBlank;

/*
 * 비밀번호 변경 요청 (POST /v1/emp/change-password)
 */
public record PasswordChangeRequest(
		@NotBlank(message = "현재 비밀번호를 입력해 주세요.") String currentPassword,
		@NotBlank(message = "새 비밀번호를 입력해 주세요.") String newPassword) {
}
