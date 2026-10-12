package com.example.gymerp.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.gymerp.dto.PtRegistrationDto;

import io.swagger.v3.oas.annotations.Parameter;

public interface PtRegistrationService {

    // ===============================
    // [기존 CRUD 기능]
    // ===============================

    // 전체 예약 목록 조회
    List<PtRegistrationDto> getAllPtRegistration(Integer empNum, String date);

    // 단일 예약 조회
    PtRegistrationDto getPtRegistrationById(int regNum);

    // 예약 등록
    int insertPtRegistration(PtRegistrationDto dto);

    // 예약 수정
    int updatePtRegistration(PtRegistrationDto dto);

    // 예약 삭제
    int deletePtRegistration(int regNum);
    
    
    
    // 스케줄번호로 예약번호 select 
    Integer findRegNumByShNum(int shNum);


    // ===============================
    // [PT LOG 관련 추가 기능]
    // ===============================

    // 회원 수정 시 회원번호 교체 
    /**
     * 기존 PT 예약의 회원 번호를 새 회원 번호로 변경합니다.
     *
     * @param regNum    변경 대상 PT 등록 번호
     * @param newMemNum 새 회원 번호
     */
    void updateRegistrationMemNum(int regNum, Long newMemNum);
    
}