package com.example.gymerp.controller;

import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gymerp.config.FileStorageProperties;
import com.example.gymerp.dto.EmpDto;
import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.security.CustomUserDetails;
import com.example.gymerp.service.EmpService;
import com.example.gymerp.service.SalesItemServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;





@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/emp") 
public class EmpController {

    private final SalesItemServiceImpl salesItemServiceImpl;
    
    private final EmpService empService;
    public final AuthenticationManager authManager;

    private final FileStorageProperties fileStorageProperties;

    // 전체 직원 목록 조회 api
    @GetMapping("/list")
    public List<EmpDto> getEmpList() {
        return empService.getAllEmp();
    }
    
    // 직원 상세 조회
    @GetMapping("/{empNum}")
    public EmpDto getEmp(@PathVariable int empNum) {
        return empService.getEmpByNum(empNum);
    }

    // 직원 등록
    @PostMapping
    public String insert(@RequestBody EmpDto dto) {
        empService.insertEmp(dto);
        return "success";
    }
    
    // 직원 수정 
    
    //  2-1) 파일 없이 정보만 수정하거나 삭제만 할 때(JSON)
    @PutMapping(value = "/{empNum}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateJson(@PathVariable int empNum, @RequestBody EmpDto dto) {
        dto.setEmpNum(empNum);
        if (Boolean.TRUE.equals(dto.getRemoveProfile())) {
            // 기존 파일 물리삭제(선택)
            EmpDto before = empService.getEmpByNum(empNum);
            String old = before != null ? before.getProfileImage() : null;
            if (old != null) {
                try {
                    Path uploadDir = fileStorageProperties.prepareUploadDir();
                    Files.deleteIfExists(uploadDir.resolve(old));
                } catch (Exception ignore) {}
            }
            dto.setProfileImage(null); // DB에 기본 이미지 상태로
        }
        empService.updateEmp(dto);
        return ResponseEntity.ok("success");
    }

    // 2-2) 새 사진까지 함께 저장할 때(멀티파트)
    @PutMapping(value = "/{empNum}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMultipart(
            @PathVariable int empNum,
            @RequestPart("emp") EmpDto dto,
            @RequestPart(value = "profileFile", required = false) MultipartFile profileFile
    ) throws IOException {
        dto.setEmpNum(empNum);

        EmpDto before = empService.getEmpByNum(empNum);
        String old = before != null ? before.getProfileImage() : null;

        if (Boolean.TRUE.equals(dto.getRemoveProfile())) {
            if (old != null) {
                try {
                    Path uploadDir = fileStorageProperties.prepareUploadDir();
                    Files.deleteIfExists(uploadDir.resolve(old));
                } catch (Exception ignore) {}
            }
            dto.setProfileImage(null);
        }

        if (profileFile != null && !profileFile.isEmpty()) {
            if (old != null) {
                try {
                    Path uploadDir = fileStorageProperties.prepareUploadDir();
                    Files.deleteIfExists(uploadDir.resolve(old));
                } catch (Exception ignore) {}
            }
            Path uploadDir = fileStorageProperties.prepareUploadDir();
            String original = profileFile.getOriginalFilename();
            String safeOriginal = StringUtils.hasText(original) ? original : "profile";
            String fileName = empNum + "_" + System.currentTimeMillis() + "_" + safeOriginal;

            Path target = uploadDir.resolve(fileName);
            profileFile.transferTo(target.toFile());

            dto.setProfileImage(fileName);
        }

        empService.updateEmp(dto);
        return ResponseEntity.ok("success");
    }

    // 직원 삭제
    @DeleteMapping("/{empNum}")
    public String delete(@PathVariable int empNum) {
        EmpDto dto = new EmpDto();
        dto.setEmpNum(empNum);
        empService.deleteEmp(dto);
        return "success";
    }
    
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody EmpDto dto, HttpServletRequest request){
    	String loginId = dto.getEmpEmail();
    	// AuthenticationManager 로 인증 시도 (Security 내부에서 UserDetailService 호출)
		Authentication auth = authManager.authenticate(
    			new UsernamePasswordAuthenticationToken(loginId, dto.getPassword()));
    	
		// 인증 결과를 SecurityContext 에 저장 (세션에도 연동)
		SecurityContext context = SecurityContextHolder.createEmptyContext();
    	context.setAuthentication(auth);
    	SecurityContextHolder.setContext(context);
    	
    	HttpSession session = request.getSession(true); // true -> 없으면 새로 생성
    	session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    	
    	// 로그인 성공 시 사용자 정보 반환
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	Map<String, Object> res = new HashMap<>();
    	res.put("empNum", user.getEmpNum());
        res.put("empName", user.getEmpName());
        res.put("email", user.getUsername());
        res.put("role", user.getRole());
        res.put("sessionId", session.getId());
        
        return ResponseEntity.ok(res);
    	
    }
    
    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
    	HttpSession session = request.getSession(false);
    	if(session != null) session.invalidate();
    	SecurityContextHolder.clearContext();
    	
    	return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
    
    // 비밀번호 변경 + 즉시 로그아웃
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody EmpDto body,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보가 없습니다."));
        }

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        empService.updatePassword(user.getEmpNum(), body.getCurrentPassword(), body.getNewPassword());

        // ★ 세션 무효화 + 시큐리티 컨텍스트 클리어 + (remember-me 있으면 쿠키도 제거)
        new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
                .logout(request, response, authentication);
        // new org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler("remember-me")
        //        .logout(request, response, authentication);

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 변경되어 재로그인이 필요합니다.",
                "requireReLogin", true
        ));
    }

    
    // 직원 검색 + 페이징 (STATUS 포함)
    @GetMapping("/list/paging")
    public Map<String, Object> getEmpListPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") String status // ACTIVE | RESIGNED | ALL
    ) {
        Map<String, Object> result = new HashMap<>();

        int start = (page - 1) * size + 1;
        int end   = page * size;

        // 상태 포함 버전을 호출
        int totalCount = empService.getTotalCount(type, keyword, status);
        List<EmpDto> list = empService.getEmpListPaged(type, keyword, status, start, end);

        int totalPage = (int) Math.ceil((double) totalCount / size);

        result.put("list", list);
        result.put("page", page);
        result.put("size", size);
        result.put("totalCount", totalCount);
        result.put("totalPage", totalPage);
        return result;
    }

    
//    // 프로필이미지 업로드
//    @PostMapping("/upload/{empNum}")
//    public ResponseEntity<String> uploadProfile(
//            @PathVariable int empNum,
//            @RequestParam("file") MultipartFile file) {
//
//        try {
//            Path uploadDir = fileStorageProperties.prepareUploadDir();
//            String original = file.getOriginalFilename();
//            String safeOriginal = StringUtils.hasText(original) ? original : "profile";
//            String fileName = empNum + "_" + System.currentTimeMillis() + "_" + safeOriginal;
//
//            Path target = uploadDir.resolve(fileName);
//            file.transferTo(target.toFile());
//
//            // DB에 파일명 저장
//            empService.updateProfileImage(empNum, fileName);
//
//            // 프론트로 반환 (React에서 미리보기용)
//            return ResponseEntity.ok(fileName);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("업로드 실패: " + e.getMessage());
//        }
//    }

    
    // 특정 직원의 회원조회 API
    @GetMapping("/{empNum}/members/pt-users")
    public List<MemberDto> managedMembersWithPt(@PathVariable int empNum) {
      return empService.selectManagedMembersWithPtBySchedule(empNum);
    }

}
