package com.example.gymerp.repository;

import java.util.List;


import com.example.gymerp.dto.MemberDto;

//주석
public interface MemberDao {

    /** 이용내역 DB 관련 내용입니다.*/
    
    // 회원 이름 단건 조회 (로그용: memNum → memName 매핑)
    String selectMemberNameById(int memNum);

    //회원 존재 여부 확인 (판매 등록 시 유효성 검증용)
    int checkMemberExists(int memNum);

	
	public List<MemberDto> selectAll(); // 전체 회원 조회 
	public MemberDto getByNum(int memNum); // 회원 상세 조회 
	
	public void insert(MemberDto dto); // 회원 등록 
	public int delete(int memNum); // 회원 삭제 
	public int update(MemberDto dto); // 회원 전체 수정 
	
	// ▼ 추가: 프로필 이미지 수정
	public int updateProfile(int memNum, String memProfile);

	// 검색
	List<MemberDto> search(String keyword);  

	
    // 담당 트레이너 / 남은 PT 횟수 / 회원권 만료일
	
    // 상태: ALL | USING | NOT_USING (null 이면 ALL)
    List<MemberDto> selectPageWithStats(String status, int startRow, int endRow);
    int countWithStats(String status);
    // 단건 상세 + 통계
    MemberDto getWithStats(int memNum);

}
