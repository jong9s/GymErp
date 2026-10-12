package com.example.gymerp.dto;

import java.util.List;

/*
 * 페이지 응답 공통 형식
 */
public record PageResponse<T>(List<T> list, int page, int size, int totalCount, int totalPage) {

	public static <T> PageResponse<T> of(List<T> list, int page, int size, int totalCount) {
		int totalPage = (int) Math.ceil((double) totalCount / size);
		return new PageResponse<>(list, page, size, totalCount, totalPage);
	}
}
