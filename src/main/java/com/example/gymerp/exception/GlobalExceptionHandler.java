package com.example.gymerp.exception;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

/*
 * 컨트롤러에서 처리되지 않은 예외를 공통 ErrorResponse(JSON)로 변환
 * - 서비스 계층은 검증 실패 시 IllegalArgumentException(잘못된 입력),
 *   IllegalStateException(현재 상태에서 불가능한 요청)을 던진다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// @Valid 검증 실패 → 400 + 필드별 사유
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fe : e.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
		}
		return respond(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요.", errors);
	}

	// 잘못된 요청 형식(JSON 파싱 실패, 필수 파라미터 누락, 타입 불일치)
	@ExceptionHandler({ HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
		return respond(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", Map.of());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
		return respond(HttpStatus.BAD_REQUEST, e.getMessage(), Map.of());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
		return respond(HttpStatus.CONFLICT, e.getMessage(), Map.of());
	}

	// 로그인 실패 (아이디/비밀번호 불일치를 구분하지 않음)
	@ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
	public ResponseEntity<ErrorResponse> handleBadCredentials(Exception e) {
		return respond(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.", Map.of());
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException e) {
		return respond(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다.", Map.of());
	}

	// 없는 경로(404), 지원하지 않는 메서드(405)/Content-Type(415)
	@ExceptionHandler({ NoResourceFoundException.class,
			HttpRequestMethodNotSupportedException.class,
			HttpMediaTypeNotSupportedException.class })
	public ResponseEntity<ErrorResponse> handleRouting(Exception e) {
		HttpStatus status = HttpStatus.valueOf(((org.springframework.web.ErrorResponse) e).getStatusCode().value());
		return respond(status, status.getReasonPhrase(), Map.of());
	}

	// ResponseStatusException 등 상태코드를 직접 지정한 예외는 그 상태코드를 그대로 사용
	@ExceptionHandler(ErrorResponseException.class)
	public ResponseEntity<ErrorResponse> handleErrorResponse(ErrorResponseException e) {
		HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
		String detail = e.getBody().getDetail();
		return respond(status, detail != null ? detail : status.getReasonPhrase(), Map.of());
	}

	@ExceptionHandler(UncheckedIOException.class)
	public ResponseEntity<ErrorResponse> handleIo(UncheckedIOException e) {
		log.error("파일 처리 중 오류", e);
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.", Map.of());
	}

	// 그 외 예상하지 못한 예외 → 500 (내부 메시지는 노출하지 않고 로그로만 남김)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		log.error("처리되지 않은 예외", e);
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", Map.of());
	}

	private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, Map<String, String> errors) {
		return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), message, errors));
	}
}
