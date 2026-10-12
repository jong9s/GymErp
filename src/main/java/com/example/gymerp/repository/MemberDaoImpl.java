package com.example.gymerp.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.example.gymerp.dto.MemberDto;

import lombok.RequiredArgsConstructor;


//주석
@Repository
@Primary
@RequiredArgsConstructor
public class MemberDaoImpl implements MemberDao {
	
	private final SqlSession session;

	// 회원 상세 조회
	@Override
	public MemberDto getByNum(int memNum) {
		return session.selectOne("MemberMapper.getByNum", memNum);
	}

	// 전체 회원 조회
	@Override
	public List<MemberDto> selectAll() {
		return session.selectList("MemberMapper.selectAll");
	}

	// 회원 등록
	@Override
	public void insert(MemberDto dto) {
		session.insert("MemberMapper.insert", dto);
	}

	// 회원 삭제 
	@Override
	public int delete(int memNum) {
		return session.update("MemberMapper.delete", memNum);
	}

	// 회원 전체 수정
	@Override
	public int update(MemberDto dto) {
		return session.update("MemberMapper.update", dto);
	}

	// ▼ 추가: 프로필 이미지 수정
	@Override
	public int updateProfile(int memNum, String memProfile) {
	    MemberDto dto = new MemberDto();
	    dto.setMemNum(memNum);
	    dto.setMemProfile(memProfile);
	    
	    return session.update("MemberMapper.updateProfile", dto);
	}
	
	/** 이용내역 DB 관련 내용입니다.*/


	@Override
	public List<MemberDto> search(String keyword) {
	    Map<String, Object> param = new HashMap<>();
	    param.put("keyword", keyword);
	    return session.selectList("MemberMapper.search", param);
	}
    // 회원 이름 단건 조회 (로그용)
    @Override
    public String selectMemberNameById(int memNum) {
        return session.selectOne("MemberMapper.selectMemberNameById", memNum);
    }

    // 회원 존재 여부 확인 (유효성 검증용)
    @Override
    public int checkMemberExists(int memNum) {
        return session.selectOne("MemberMapper.checkMemberExists", memNum);
    }

    @Override
    public List<MemberDto> selectPageWithStats(String status, int startRow, int endRow) {
        Map<String, Object> p = new HashMap<>();
        p.put("status", status);
        p.put("startRow", startRow);
        p.put("endRow", endRow);
        return session.selectList("MemberMapper.selectPageWithStats", p);
    }

    @Override
    public int countWithStats(String status) {
        return session.selectOne("MemberMapper.countWithStats", Map.of("status", status));
    }

    @Override
    public MemberDto getWithStats(int memNum) {
        return session.selectOne("MemberMapper.getWithStats", memNum);
    }
}
