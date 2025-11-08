package com.example.gymerp.dto;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberDto {
	 private int memNum;              // 회원 고유번호 (PK)
	 private String memName;          // 회원 이름
	 private String memGender;        // 성별
	 private Date memBirthday;        // 생일
	 private String memPhone;         // 연락처
	 private String memEmail;         // 이메일
	 private String memAddr;          // 주소
	 private Timestamp memCreated;    // 등록일
	 private Timestamp memUpdated;    // 수정일
	 private String memProfile;       // 프로필 이미지 파일명 또는 URL
	 private String memNote;          // 메모

	// ▼ 계산/조인으로 채우는 확장 필드
	private String  trainerName;       // employee.empName (last trainer by pt_log)
	private Date    voucherStartDate;  // 최신권 시작일
	private Date    voucherEndDate;    // 최신권 만료일
	private Integer ptRemain;          // pt_log.countChange 합계
	private String  membershipStatus;  // '사용중' | '미사용중'
	
	// *추가 직원상세-회원목록으로 로딩할 값
	private Integer usedCount;   // 소비(사용) 회수
	private Timestamp lastUseAt; // 마지막 사용일(소비 기준)
}
