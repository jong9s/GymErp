package com.example.gymerp.service;

import java.util.List;

import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.dto.PageResponse;

//주석
public interface MemberService {
    // 회원 추가
    public void createMember(MemberDto dto);

    // 회원 수정
    public void updateMember(MemberDto dto);

    // 회원 삭제
    public void deleteMember(int memNum);

    // 회원 상세 조회
    public MemberDto getMember(int memNum);

    // 회원 목록 조회
    public List<MemberDto> getAllMembers();
    
    // ▼ 추가: 프로필 이미지 수정 (파일명/경로만 업데이트)
    public int updateMemberProfile(int memNum, String memProfile);
    
    //검색
    List<MemberDto> searchMembers(String keyword); 


    // 담당 트레이너 / 남은 PT 횟수 / 회원권 만료일
    PageResponse<MemberDto> getPageWithStats(String status, int page, int size);
    MemberDto getWithStats(int memNum);
  
	
}
