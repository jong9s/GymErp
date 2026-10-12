package com.example.gymerp.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.dto.PageResponse;
import com.example.gymerp.repository.MemberDao;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
	private final MemberDao memberDao;
	
	// 회원 추가
    @Override
    @Transactional
    public void createMember(MemberDto dto) {
        memberDao.insert(dto);
    }

    // 회원 수정
    @Override
    @Transactional
    public void updateMember(MemberDto dto) {
        memberDao.update(dto);
    }

    // 회원 삭제
    @Override
    @Transactional
    public void deleteMember(int memNum) {
        memberDao.delete(memNum);
    }

    // 회원 상세 조회
    @Override
    @Transactional(readOnly = true)
    public MemberDto getMember(int memNum) {
        return memberDao.getByNum(memNum);
    }

    // 회원 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<MemberDto> getAllMembers() {
        return memberDao.selectAll();
    }

    // ▼ 추가: 프로필 이미지 수정
    @Override
    @Transactional
    public int updateMemberProfile(int memNum, String memProfile) {
        return memberDao.updateProfile(memNum, memProfile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MemberDto> searchMembers(String keyword) {
        return memberDao.search(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberDto> getPageWithStats(String status, int page, int size) {
        String normalized = status == null ? "ALL" : status.toUpperCase(Locale.ROOT);
        int startRow = (page - 1) * size;
        List<MemberDto> list = memberDao.selectPageWithStats(normalized, startRow, startRow + size);
        int totalCount = memberDao.countWithStats(normalized);
        return PageResponse.of(list, page, size, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDto getWithStats(int memNum) {
        return memberDao.getWithStats(memNum);
    }

}
