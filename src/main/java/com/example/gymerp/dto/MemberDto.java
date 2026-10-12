package com.example.gymerp.dto;

import java.sql.Date;
import java.sql.Timestamp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberDto {
	private int memNum;              // 회원 고유번호 (PK)

	@NotBlank(message = "이름은 필수입니다.")
	@Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
	private String memName;          // 회원 이름

	@NotBlank(message = "성별은 필수입니다.")
	private String memGender;        // 성별

	@NotNull(message = "생년월일은 필수입니다.")
	private Date memBirthday;        // 생일

	@NotBlank(message = "연락처는 필수입니다.")
	@Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
	private String memPhone;         // 연락처

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
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
