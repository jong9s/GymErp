package com.example.gymerp.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymerp.dto.EmpDto;
import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.repository.EmpDao;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpServiceImpl implements EmpService {

    private final EmpDao empDao;
    private final PasswordEncoder passwordEncoder;

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("\\D", "");
    }

    @Override
    public List<EmpDto> getAllEmp() {
        return empDao.getAllEmp();
    }

    @Override
    public EmpDto getEmpByNum(int empNum) {
        return empDao.getEmpByNum(empNum);
    }

    @Override
    public int insertEmp(EmpDto dto) {
        // 0) 이메일 중복 사전검증
        if (empDao.existsByEmail(dto.getEmpEmail()) > 0) {
            throw new IllegalArgumentException("중복된 이메일입니다.");
        }

        // 1) 전화번호 정규화
        String digits = normalizePhone(dto.getEmpPhone());
        dto.setEmpPhone(digits);

        // 2) 비밀번호(전화번호 뒤 4자리) → BCrypt
        if (digits != null && digits.length() >= 4) {
            String rawPwd = digits.substring(digits.length() - 4);
            dto.setPassword(passwordEncoder.encode(rawPwd));
        } else {
            throw new IllegalArgumentException("유효하지 않은 전화번호 방식입니다");
        }

        // 3) INSERT + 제약위반 사용자 메시지 변환
        try {
            return empDao.insertEmp(dto);
        } catch (DataIntegrityViolationException ex) {
            if (isEmailUniqueViolation(ex)) {
                throw new IllegalArgumentException("중복된 이메일입니다.");
            }
            throw ex;
        }
    }

    private boolean isEmailUniqueViolation(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null && (msg.contains("EMPEMAIL")
                    || msg.contains("EMAIL")
                    || msg.contains("UQ_EMPLOYEE_EMPEMAIL")
                    || msg.contains("SYS_C"))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    @Override
    public int updateEmp(EmpDto dto) {
        return empDao.updateEmp(dto);
    }

    @Override
    public int deleteEmp(EmpDto dto) {
        // 실제 운영에선 보통 사용 안 함(참조무결성). 화면에선 resign만 노출 권장.
        return empDao.deleteEmp(dto);
    }

    @Override
    public List<EmpDto> searchEmp(String keyword, String filter) {
        return empDao.searchEmp(keyword, filter);
    }

    @Override
    public EmpDto login(String email, String password) {
        EmpDto emp = empDao.selectAuthByEmail(email);
        if (emp == null) throw new IllegalArgumentException("이메일이 존재하지 않습니다");
        if (!passwordEncoder.matches(password, emp.getPassword()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        emp.setPassword(null);
        return emp;
    }

    @Override
    public void updatePassword(int empNum, String currentPassword, String newPassword) {
        String dbHash = empDao.selectPasswordHashByEmpNum(empNum);
        if (dbHash == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        if (!passwordEncoder.matches(currentPassword, dbHash))
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        if (passwordEncoder.matches(newPassword, dbHash))
            throw new IllegalArgumentException("이전과 동일한 비밀번호는 사용할 수 없습니다.");

        int updated = empDao.updatePassword(empNum, passwordEncoder.encode(newPassword));
        if (updated != 1) throw new IllegalArgumentException("비밀번호 변경에 실패했습니다");
    }

    // status 포함 페이징만 유지
    @Override
    public List<EmpDto> getEmpListPaged(String type, String keyword, String status, int start, int end) {
        return empDao.getEmpListPaged(type, keyword, status, start, end);
    }

    @Override
    public int getTotalCount(String type, String keyword, String status) {
        return empDao.getTotalCount(type, keyword, status);
    }

    @Override
    public void updateProfileImage(int empNum, String fileName) {
        empDao.updateProfileImage(empNum, fileName);
    }

    // 퇴사 처리(소프트 삭제)
    @Override
    public void resign(int empNum, String reason) {
        int updated = empDao.markResigned(empNum, reason);
        if (updated != 1) throw new IllegalArgumentException("퇴사 처리 실패 또는 대상이 없습니다.");
    }
    
    // 기존(구버전) 시그니처 호환용: 상태 기본값은 ALL 로 위임
    @Override
    public List<EmpDto> getEmpListPaged(String type, String keyword, int start, int end) {
        return getEmpListPaged(type, keyword, "ALL", start, end);
    }

    @Override
    public int getTotalCount(String type, String keyword) {
        return getTotalCount(type, keyword, "ALL");
    }
    
    // 특정 직원의 회원 조회(PT 잔여수가 있는 경우)
	@Override
	public List<MemberDto> selectManagedMembersWithPt(int empNum) {
		return empDao.selectManagedMembersWithPt(empNum);
	}
	
	// 특정 직원의 회원 조회(PT 스케줄이 등록된 경우)
	@Override
	public List<MemberDto> selectManagedMembersWithPtBySchedule(int empNum) {
		return empDao.selectManagedMembersWithPtBySchedule(empNum);
	}

}
