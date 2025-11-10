package com.example.gymerp.dto;

import java.sql.Date;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Alias("EmpDto")
public class EmpDto {
	private Integer empNum;           // 직원 고유번호 (PK)
    private String empName;       // 이름
    private String gender;        // 성별
    private String empAddress;    // 주소
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date empBirth;        // 생년월일
    
    private String empPhone;      // 연락처
    private String empEmail;      // 이메일
    private String password;      // 비밀번호 2025-10-30 13:07 추가
    private String currentPassword;   // 비밀번호 변경 요청 시 현재 비번 (요청에서만 받음)
    private String newPassword;       // 비밀번호 변경 요청 시 새 비번 (요청에서만 받음)
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date hireDate;        // 입사일
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fireDate;        // 퇴사(예정)일
    
    private String profileImage;  // 프로필 사진 파일명
    private Boolean removeProfile; // 프로필 사진 삭제
    
    private String empMemo;       // 메모
    private String role;          // 직급 (EMP, ADMIN)
    
    
}
