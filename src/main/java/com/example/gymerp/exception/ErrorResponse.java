package com.example.gymerp.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/*
 * 공통 에러 응답 형식
 * { "status": 400, "message": "...", "errors": { "필드": "사유" }, "timestamp": "..." }
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {

	public static ErrorResponse of(int status, String message) {
		return new ErrorResponse(status, message, Map.of(), LocalDateTime.now());
	}

	public static ErrorResponse of(int status, String message, Map<String, String> errors) {
		return new ErrorResponse(status, message, errors, LocalDateTime.now());
	}
}
