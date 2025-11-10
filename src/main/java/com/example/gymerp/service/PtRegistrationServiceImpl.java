package com.example.gymerp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymerp.dto.PtLogDto;
import com.example.gymerp.dto.PtRegistrationDto;
import com.example.gymerp.dto.ScheduleDto;
import com.example.gymerp.repository.LogDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PtRegistrationServiceImpl implements PtRegistrationService {

    private final SqlSession session;
    private final SqlSessionTemplate sessionT;   // Spring 관리형 SqlSessionTemplate (Thread-safe)

    
    // 전체 예약 목록 조회
    @Override
    public List<PtRegistrationDto> getAllPtRegistration(Integer empNum, String date) {
        // Mapper에서 모든 REGISTRATION 데이터 조회
        return session.selectList("PtRegistrationMapper.getAllPtRegistration");
    }

    
    // 단일 예약 조회
    @Override
    public PtRegistrationDto getPtRegistrationById(int regNum) {
        return session.selectOne("com.example.gymerp.repository.PtRegistrationMapper.getPtRegistrationById", regNum);
    }


    // 예약 등록 (+ PT_LOG 소비 -1)
    @Override
    @Transactional
    public int insertPtRegistration(PtRegistrationDto dto) {
    	int rows = session.insert("PtRegistrationMapper.insertPtRegistration", dto);
        System.out.println("[PT 예약 등록 완료] regNum=" + dto.getRegNum());
        return rows;
    }



    // 예약 수정
    @Override
    public int updatePtRegistration(PtRegistrationDto dto) {
    	return session.update("PtRegistrationMapper.updatePtRegistration", dto);
    }


    // 예약 삭제 (+ PT_LOG 복구 +1)
    @Override
    @Transactional
    public int deletePtRegistration(int regNum) {
        // 1️⃣ 삭제 대상 조회
        PtRegistrationDto target = session.selectOne("PtRegistrationMapper.getPtRegistrationById", regNum);
        if (target == null) {
            throw new IllegalArgumentException("존재하지 않는 PT 등록입니다.");
        }

        System.out.println("[PT 예약 삭제 시작] regNum=" + regNum);

        // 2️⃣ PT 등록 데이터 삭제
        int deleted = session.delete("PtRegistrationMapper.deletePtRegistration", regNum);
        System.out.println("[PT 예약 삭제 완료] deleted=" + deleted);

        return deleted;
    }

    //일정번호(shNum)로 PT 등록번호(regNum) 조회 
    @Override
    public Integer findRegNumByShNum(int shNum) {
        System.out.println("[findRegNumByShNum 호출] shNum=" + shNum);
    return sessionT.selectOne("PtRegistrationMapper.findRegNumByShNum", shNum);
    }

    // 등록된 회원 번호 변경
	@Override
	public void updateRegistrationMemNum(int regNum, Long newMemNum) {
		if(newMemNum == null) {
			throw new IllegalArgumentException("수정할 회원을 지정해야합니다");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("regNum", regNum);
		params.put("newMemNum", newMemNum);
		int updated = session.update("PtRegistrationMapper.updateRegistrationMemNum", params);
		if(updated == 0) {
			throw new IllegalStateException("PT 등록번호" + regNum + "에 대한 업데이트가 실패했습니다");
		}
		
	}

	
}