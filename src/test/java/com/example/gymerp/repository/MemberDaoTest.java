package com.example.gymerp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.gymerp.dto.MemberDto;

/**
 * MemberMapper.xml SQL 검증 (H2, Oracle 호환 모드)
 * - 초기 데이터: data-h2.sql
 */
@MybatisTest
@ActiveProfiles("test")
@Import(MemberDaoImpl.class)
class MemberDaoTest {

    @Autowired
    private MemberDao memberDao;

    private MemberDto newMember(String phone, String email) {
        MemberDto dto = new MemberDto();
        dto.setMemName("테스트회원");
        dto.setMemGender("여");
        dto.setMemBirthday(Date.valueOf("2000-01-01"));
        dto.setMemPhone(phone);
        dto.setMemEmail(email);
        return dto;
    }

    @Test
    @DisplayName("insert - 시퀀스로 회원번호를 발급하고 DTO 에 채워 준다")
    void insert_assignsMemNum() {
        MemberDto dto = newMember("010-9999-0001", "new1@test.com");

        memberDao.insert(dto);

        assertThat(dto.getMemNum()).isPositive();
        MemberDto saved = memberDao.getByNum(dto.getMemNum());
        assertThat(saved.getMemName()).isEqualTo("테스트회원");
        assertThat(saved.getMemEmail()).isEqualTo("new1@test.com");
    }

    @Test
    @DisplayName("update - 전체 필드를 수정한다")
    void update_changesFields() {
        MemberDto dto = newMember("010-9999-0002", "new2@test.com");
        memberDao.insert(dto);

        dto.setMemName("이름변경");
        dto.setMemNote("메모");
        int updated = memberDao.update(dto);

        assertThat(updated).isEqualTo(1);
        MemberDto saved = memberDao.getByNum(dto.getMemNum());
        assertThat(saved.getMemName()).isEqualTo("이름변경");
        assertThat(saved.getMemNote()).isEqualTo("메모");
    }

    @Test
    @DisplayName("search - 이름/연락처/이메일 일부로 대소문자 구분 없이 검색한다")
    void search_byKeyword() {
        memberDao.insert(newMember("010-9999-0003", "FindMe@test.com"));

        List<MemberDto> byEmail = memberDao.search("findme");
        List<MemberDto> byPhone = memberDao.search("9999-0003");

        assertThat(byEmail).extracting(MemberDto::getMemEmail).contains("FindMe@test.com");
        assertThat(byPhone).hasSize(1);
    }

    @Test
    @DisplayName("updateProfile - 프로필 파일명만 변경한다")
    void updateProfile() {
        MemberDto dto = newMember("010-9999-0004", "new4@test.com");
        memberDao.insert(dto);

        memberDao.updateProfile(dto.getMemNum(), "x_1.png");

        assertThat(memberDao.getByNum(dto.getMemNum()).getMemProfile()).isEqualTo("x_1.png");
    }
}
