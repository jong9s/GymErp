package com.example.gymerp.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.example.gymerp.dto.EmpDto;
import com.example.gymerp.dto.MemberDto;

import lombok.RequiredArgsConstructor;

@Repository
@Primary
@RequiredArgsConstructor
public class EmpDaoImpl implements EmpDao {

    private final SqlSession session;

    @Override
    public List<EmpDto> getAllEmp() {
        return session.selectList("EmployeeMapper.getAllEmp");
    }

    @Override
    public EmpDto getEmpByNum(int empNum) {
        return session.selectOne("EmployeeMapper.getEmpByNum", empNum);
    }

    @Override
    public int insertEmp(EmpDto dto) {
        return session.insert("EmployeeMapper.insertEmp", dto);
    }

    @Override
    public int updateEmp(EmpDto dto) {
        return session.update("EmployeeMapper.updateEmp", dto);
    }

    @Override
    public int deleteEmp(EmpDto dto) {
        return session.delete("EmployeeMapper.deleteEmp", dto);
    }

    @Override
    public List<EmpDto> searchEmp(String keyword, String filter) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("filter", filter);
        return session.selectList("EmployeeMapper.searchEmp", params);
    }

    @Override
    public EmpDto selectAuthByEmail(String empEmail) {
        return session.selectOne("EmployeeMapper.selectAuthByEmail", empEmail);
    }

    @Override
    public int updatePassword(int empNum, String hashed) {
        Map<String, Object> params = new HashMap<>();
        params.put("empNum", empNum);
        params.put("password", hashed);
        return session.update("EmployeeMapper.updatePassword", params);
    }

    @Override
    public String selectPasswordHashByEmpNum(int empNum) {
        return session.selectOne("EmployeeMapper.selectPasswordHashByEmpNum", empNum);
    }

    // status 포함 페이징
    @Override
    public List<EmpDto> getEmpListPaged(String type, String keyword, String status, int start, int end) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("keyword", keyword);
        params.put("status", status); // ACTIVE | RESIGNED | ALL
        params.put("start", start);
        params.put("end", end);
        return session.selectList("EmployeeMapper.getEmpListPaged", params);
    }

    @Override
    public int getTotalCount(String type, String keyword, String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("keyword", keyword);
        params.put("status", status);
        return session.selectOne("EmployeeMapper.getTotalCount", params);
    }

    @Override
    public void updateProfileImage(int empNum, String fileName) {
        Map<String, Object> params = new HashMap<>();
        params.put("empNum", empNum);
        params.put("fileName", fileName);
        session.update("EmployeeMapper.updateProfileImage", params);
    }

    @Override
    public String selectEmployeeNameById(int empNum) {
        return session.selectOne("EmployeeMapper.selectEmployeeNameById", empNum);
    }

    @Override
    public int checkEmployeeExists(int empNum) {
        return session.selectOne("EmployeeMapper.checkEmployeeExists", empNum);
    }

    @Override
    public int existsByEmail(String email) {
        return session.selectOne("EmployeeMapper.existsByEmail", email);
    }

    @Override
    public int markResigned(int empNum, String reason) {
        Map<String, Object> params = new HashMap<>();
        params.put("empNum", empNum);
        params.put("reason", reason);
        return session.update("EmployeeMapper.markResigned", params);
    }

    // EmpDaoImpl
    @Override
    public List<EmpDto> getEmpListPaged(String type, String keyword, int start, int end) {
        return getEmpListPaged(type, keyword, "ALL", start, end);
    }

    @Override
    public int getTotalCount(String type, String keyword) {
        return getTotalCount(type, keyword, "ALL");
    }

	@Override
	public List<MemberDto> selectManagedMembersWithPt(int empNum) {
		return session.selectList("EmployeeMapper.selectManagedMembersWithPt", empNum);
	}

	@Override
	public List<MemberDto> selectManagedMembersWithPtBySchedule(int empNum) {
		return session.selectList("EmployeeMapper.selectManagedMembersWithPtBySchedule", empNum);
	}

}

