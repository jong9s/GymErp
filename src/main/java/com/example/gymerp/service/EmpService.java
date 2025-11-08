package com.example.gymerp.service;

import java.util.List;
import com.example.gymerp.dto.EmpDto;
import com.example.gymerp.dto.MemberDto;

public interface EmpService {
	
	// 전체 직원 목록 조회
    public List<EmpDto> getAllEmp();
    
    // 직원 1명 조회
    public EmpDto getEmpByNum(int empNum);
    
    // 직원 등록
    public int insertEmp(EmpDto dto);
    
    // 직원 정보 수정
    public int updateEmp(EmpDto dto);
    
    // 직원 삭제 
    public int deleteEmp(EmpDto dto);
    
    // 직원 검색
    public List<EmpDto> searchEmp(String keyword, String filter);
    
    // 로그인 (이메일 + 비밀번호)
    public EmpDto login(String email, String password);
    
    // 비밀번호 변경 (선택)
    public void updatePassword(int empNum, String currentPassword, String newPassword);
    
    // 직원 검색 + 페이징
    public List<EmpDto> getEmpListPaged(String type, String keyword, int start, int end);

    // 직원 총 개수
    public int getTotalCount(String type, String keyword);
    
    // 프로필이미지
    public void updateProfileImage(int empNum, String fileName);
    
    // 상태 필터: ACTIVE(재직) | RESIGNED(퇴사) | ALL(전체)
    public List<EmpDto> getEmpListPaged(String type, String keyword, String status, int start, int end);
    public int getTotalCount(String type, String keyword, String status);
    
    // 퇴사 처리(소프트 삭제)
    public void resign(int empNum, String reason);
    
    // 특정 직원의 회원조회
    List<MemberDto> selectManagedMembersWithPt(int empNum);
    List<MemberDto> selectManagedMembersWithPtBySchedule(int empNum);
}