package com.example.gymerp.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.gymerp.dto.EmpDto;
import com.example.gymerp.dto.MemberDto;

public interface EmpDao {
	
    List<EmpDto> getAllEmp();
    EmpDto getEmpByNum(@Param("empNum") int empNum);
    int insertEmp(EmpDto dto);
    int updateEmp(EmpDto dto);
    int deleteEmp(EmpDto dto);

    List<EmpDto> searchEmp(@Param("keyword") String keyword,
                           @Param("filter") String filter);

    EmpDto selectAuthByEmail(String email);
    String selectPasswordHashByEmpNum(int empNum);
    int updatePassword(@Param("empNum") int empNum, @Param("hashed") String hashed);

    //  status 포함
    List<EmpDto> getEmpListPaged(@Param("type") String type,
                                 @Param("keyword") String keyword,
                                 @Param("status") String status,
                                 @Param("start") int start,
                                 @Param("end") int end);

    int getTotalCount(@Param("type") String type,
                      @Param("keyword") String keyword,
                      @Param("status") String status);
    
    
    // 구버전(호환용): 컨트롤러가 아직 status 안 넘길 때 대비
    List<EmpDto> getEmpListPaged(@Param("type") String type,
                                 @Param("keyword") String keyword,
                                 @Param("start") int start,
                                 @Param("end") int end);
    
    int getTotalCount(@Param("type") String type,
            @Param("keyword") String keyword);
    
    
    void updateProfileImage(int empNum, String fileName);

    String selectEmployeeNameById(int empNum);
    int checkEmployeeExists(int empNum);

    int existsByEmail(String email);
    
    // 퇴사 처리
    int markResigned(@Param("empNum") int empNum, @Param("reason") String reason);
    
    // 특정 직원의 회원 조회(Pt 및 스케줄이 SCHEDULE-PT 인 경우)
    List<MemberDto> selectManagedMembersWithPt(int empNum);
    List<MemberDto> selectManagedMembersWithPtBySchedule(int empNum);
}
