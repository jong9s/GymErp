package com.example.gymerp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.service.FileStorageService;
import com.example.gymerp.service.MemberService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

/**
 * ✅ MemberController (회원 관리 컨트롤러)
 * - 프론트엔드 axios 요청과 매핑되는 API 엔드포인트 정의
 * - CRUD + 검색 + 프로필 업로드 기능 포함
 */
@RequestMapping("/v1")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final FileStorageService fileStorageService;

    // ✅ (1) 회원 목록 조회 (담당트레이너/남은PT/회원권/상태 포함)
    // 프론트 요청: GET /v1/member?status=ALL
    @GetMapping("/member")
    public List<MemberDto> list(@RequestParam(required=false, defaultValue="ALL") String status) {
        return memberService.getAllWithStats(status);
    }

    // ✅ (2) 회원 상세 조회
    // 프론트 요청: GET /v1/member/{memNum}
    @GetMapping("/member/{memNum}")
    public MemberDto detail(@PathVariable int memNum) {
        return memberService.getWithStats(memNum);
    }

    // ✅ (3) 회원 등록
    // 프론트 요청: POST /v1/member
    @PostMapping("/member")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody MemberDto dto) {
        memberService.createMember(dto);
        return ResponseEntity.ok(Map.of("memNum", dto.getMemNum()));
    }

    // ✅ (4) 회원 수정 (전체 필드 교체)
    // 프론트 요청: PUT /v1/member/{memNum}
    @PutMapping("/member/{memNum}")
    public ResponseEntity<Void> update(@PathVariable int memNum, @Valid @RequestBody MemberDto dto) {
        dto.setMemNum(memNum);
        memberService.updateMember(dto);
        return ResponseEntity.noContent().build();
    }

    // ✅ (5) 회원 삭제
    // 프론트 요청: DELETE /v1/member/{memNum}
    @DeleteMapping("/member/{memNum}")
    public ResponseEntity<Void> delete(@PathVariable int memNum) {
        memberService.deleteMember(memNum);
        return ResponseEntity.noContent().build();
    }

    // ✅ (6) 회원 검색
    // 프론트 요청: GET /v1/member/search?keyword=값
    @GetMapping("/member/search")
    public List<MemberDto> search(@RequestParam String keyword) {
        return memberService.searchMembers(keyword);
    }

    // ✅ (7) 회원 프로필만 수정 (문자열 경로 기반)
    // 프론트 요청: PATCH /v1/member/{memNum}/profile
    @PatchMapping("/member/{memNum}/profile")
    public ResponseEntity<Void> updateProfile(@PathVariable int memNum, @RequestBody MemberDto dto) {
        memberService.updateMemberProfile(memNum, dto.getMemProfile());
        return ResponseEntity.noContent().build();
    }

    // ✅ (8) 회원 프로필 이미지 업로드 → 저장된 파일명 반환
    // 프론트 요청: POST /v1/member/upload/{memNum} (multipart, file)
    @PostMapping("/member/upload/{memNum}")
    public ResponseEntity<String> uploadMemberProfile(@PathVariable int memNum,
                                                      @RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeImage(file, String.valueOf(memNum));
        memberService.updateMemberProfile(memNum, fileName);
        return ResponseEntity.ok(fileName);
    }

}
