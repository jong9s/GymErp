package com.example.gymerp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gymerp.dto.ServiceDto;
import com.example.gymerp.dto.ServiceListResponse;
import com.example.gymerp.service.ServiceService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class ServiceController {

	private final ServiceService serviceService;
	
	//상품 목록 조회
	@GetMapping("/service")
	public ServiceListResponse getServiceList(
			@RequestParam(defaultValue = "1") int pageNum, 
			ServiceDto dto,
			@RequestParam(defaultValue = "codeBName") String sortBy,
			@RequestParam(defaultValue = "ASC") String direction
	) {
		
		return serviceService.getServices(pageNum, dto, sortBy, direction);
	}
	
	//서비스 상품 등록
	@PostMapping("/service")
	public void createService(@RequestBody ServiceDto dto) {
		serviceService.save(dto);
	}
	
	//서비스 상품 수정
	@PutMapping("/service/{serviceId}")
	public ServiceDto updateService(@RequestBody ServiceDto dto, @PathVariable int serviceId) {
		dto.setServiceId(serviceId);
		//path variable 이 없는 방식(dto 에 product id 를 담아서 보내주는 방식)을 시도해보고 된다면 path variable 지우기
		serviceService.modifyService(dto);
		
		return dto;
	}
	
	//서비스 상품 상세정보 조회
	@GetMapping("/service/{serviceId}")
	public ServiceDto getServiceDetail(@PathVariable int serviceId) {
		ServiceDto result = serviceService.getDetail(serviceId);
		
		return result;
	}
	
	//서비스 상품 판매 상태 변경
	@PatchMapping("/service/{serviceId}")
	public ResponseEntity<Map<String, Object>> updateProductStatus(
			@PathVariable int serviceId, 
            @RequestBody ServiceDto dto
			){
		if (dto.getIsActive() == null) {
            return ResponseEntity.badRequest().build(); // 간단한 유효성 검사
        }
		
		serviceService.updateServiceStatus(serviceId, dto.getIsActive());
		Map<String, Object> responseBody = Map.of("message", "판매 상태 변경에 성공했습니다.");
		return ResponseEntity.ok(responseBody);
  }
}
