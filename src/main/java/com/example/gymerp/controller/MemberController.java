package com.example.gymerp.controller;

import java.nio.file.Path;
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
import org.springframework.util.StringUtils;

import com.example.gymerp.config.FileStorageProperties;
import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.service.MemberService;

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
    private final FileStorageProperties fileStorageProperties;

    
    @GetMapping("/member")
    public List<MemberDto> list(@RequestParam(required=false, defaultValue="ALL") String status) {
        return memberService.getAllWithStats(status); // ← 확장 필드 포함(담당트레이너/남은PT/회원권/상태)
    }

    @GetMapping("/member/{memNum}")
    public MemberDto detail(@PathVariable int memNum) {
        return memberService.getWithStats(memNum); // ← 단건도 확장 필드 포함
    }
    
    

    // ✅ (3) 회원 등록
    // 프론트 요청: POST /v1/member
    @PostMapping("/member")
    public ResponseEntity<Map<String, Object>> create(@RequestBody MemberDto dto) {
        memberService.createMember(dto);
        return ResponseEntity.ok(Map.of("memNum", dto.getMemNum()));
    }

    // ✅ (4) 회원 수정 (PUT 요청용)
    // 프론트 요청: PUT /v1/member/{memNum}
    @PutMapping("/member/{memNum}")
    public ResponseEntity<Void> updatePut(@PathVariable int memNum, @RequestBody MemberDto dto) {
        dto.setMemNum(memNum);
        memberService.updateMember(dto);
        return ResponseEntity.noContent().build();
    }

    // ✅ (5) 회원 수정 (PATCH 요청용)
    // 일부 필드만 변경할 때 사용 가능
    @PatchMapping("/member/{memNum}")
    public ResponseEntity<Void> updatePatch(@PathVariable int memNum, @RequestBody MemberDto dto) {
        dto.setMemNum(memNum);
        memberService.updateMember(dto);
        return ResponseEntity.noContent().build();
    }

    // ✅ (6) 회원 삭제
    // 프론트 요청: DELETE /v1/member/{memNum}
    @DeleteMapping("/member/{memNum}")
    public ResponseEntity<Void> delete(@PathVariable int memNum) {
        memberService.deleteMember(memNum);
        return ResponseEntity.noContent().build();
    }

    // ✅ (7) 회원 검색
    // 프론트 요청: GET /v1/member/search?keyword=값
    @GetMapping("/member/search")
    public List<MemberDto> search(@RequestParam String keyword) {
        return memberService.searchMembers(keyword);
    }

    // ✅ (8) 회원 프로필만 수정 (문자열 경로 기반)
    // 프론트 요청: PATCH /v1/member/{memNum}/profile
    @PatchMapping("/member/{memNum}/profile")
    public ResponseEntity<Void> updateProfile(@PathVariable int memNum, @RequestBody MemberDto dto) {
        memberService.updateMemberProfile(memNum, dto.getMemProfile());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/member/upload/{memNum}")
    public ResponseEntity<String> uploadMemberProfile(@PathVariable int memNum,
                                                      @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어있습니다.");
        }
        try {
            Path uploadDir = fileStorageProperties.prepareUploadDir();
            String original = file.getOriginalFilename();
            String safeOriginal = StringUtils.hasText(original) ? original : "profile";
            String ext = "";
            int dotIndex = safeOriginal.lastIndexOf('.');
            if (dotIndex != -1) {
                ext = safeOriginal.substring(dotIndex);
            }
            String fileName = memNum + "_" + System.currentTimeMillis() + ext;

            Path target = uploadDir.resolve(fileName);
            file.transferTo(target.toFile());

            // 4) DB 업데이트
            memberService.updateMemberProfile(memNum, fileName);

            // 5) 프론트로 파일명 반환
            return ResponseEntity.ok(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 실패: " + e.getMessage());
        }
    }

}
