package com.example.gymerp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

import com.example.gymerp.dto.EmpDto;
import com.example.gymerp.dto.LoginRequest;
import com.example.gymerp.dto.MemberDto;
import com.example.gymerp.dto.PasswordChangeRequest;
import com.example.gymerp.security.CustomUserDetails;
import com.example.gymerp.service.EmpService;
import com.example.gymerp.service.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/emp") 
public class EmpController {

    private final EmpService empService;
    private final AuthenticationManager authManager;
    private final FileStorageService fileStorageService;

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
            fileStorageService.deleteQuietly(currentProfileImage(empNum));
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
    ) {
        dto.setEmpNum(empNum);
        String old = currentProfileImage(empNum);

        if (Boolean.TRUE.equals(dto.getRemoveProfile())) {
            fileStorageService.deleteQuietly(old);
            dto.setProfileImage(null);
        }

        if (profileFile != null && !profileFile.isEmpty()) {
            String fileName = fileStorageService.storeImage(profileFile, String.valueOf(empNum));
            fileStorageService.deleteQuietly(old);
            dto.setProfileImage(fileName);
        }

        empService.updateEmp(dto);
        return ResponseEntity.ok("success");
    }

    private String currentProfileImage(int empNum) {
        EmpDto before = empService.getEmpByNum(empNum);
        return before != null ? before.getProfileImage() : null;
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto, HttpServletRequest request){
    	// AuthenticationManager 로 인증 시도 (Security 내부에서 UserDetailService 호출)
    	// 실패 시 BadCredentialsException → GlobalExceptionHandler 에서 401 응답
		Authentication auth = authManager.authenticate(
    			new UsernamePasswordAuthenticationToken(dto.empEmail(), dto.password()));
    	
		// 인증 결과를 SecurityContext 에 저장 (세션에도 연동)
		SecurityContext context = SecurityContextHolder.createEmptyContext();
    	context.setAuthentication(auth);
    	SecurityContextHolder.setContext(context);
    	
    	// 세션 고정 공격 방지: 로그인 전 세션이 있으면 폐기하고 새 세션 발급
    	HttpSession oldSession = request.getSession(false);
    	if (oldSession != null) oldSession.invalidate();
    	HttpSession session = request.getSession(true);
    	session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    	
    	// 로그인 성공 시 사용자 정보 반환
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	Map<String, Object> res = new HashMap<>();
    	res.put("empNum", user.getEmpNum());
        res.put("empName", user.getEmpName());
        res.put("email", user.getUsername());
        res.put("role", user.getRole());
        
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
            @Valid @RequestBody PasswordChangeRequest body,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보가 없습니다."));
        }

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        empService.updatePassword(user.getEmpNum(), body.currentPassword(), body.newPassword());

        // 비밀번호 변경 후 세션 무효화 + 시큐리티 컨텍스트 클리어 → 재로그인 유도
        new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
                .logout(request, response, authentication);

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

    

    
    // 특정 직원의 회원조회 API
    @GetMapping("/{empNum}/members/pt-users")
    public List<MemberDto> managedMembersWithPt(@PathVariable int empNum) {
      return empService.selectManagedMembersWithPtBySchedule(empNum);
    }

}
