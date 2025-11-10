package com.example.gymerp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.gymerp.dto.ServiceDto;
import com.example.gymerp.dto.ServiceListResponse;
import com.example.gymerp.repository.ServiceDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService{

	private final ServiceDao serviceDao;

	@Override
	public ServiceListResponse getServices(int pageNum, ServiceDto dto, String sortBy, String direction) {
		
		//한 페이지에 몇개씩 표시할 것인지
		final int PAGE_ROW_COUNT=10;
		
		//하단 페이지를 몇개씩 표시할 것인지
		final int PAGE_DISPLAY_COUNT=5;

		//보여줄 페이지의 시작 ROWNUM
		int startRowNum=1+(pageNum-1)*PAGE_ROW_COUNT; //공차수열
		//보여줄 페이지의 끝 ROWNUM
		int endRowNum=pageNum*PAGE_ROW_COUNT; //등비수열 
		
		//하단 시작 페이지 번호 (정수를 정수로 나누면 소수점이 버려진 정수가 나온다)
		int startPageNum = 1 + ((pageNum-1)/PAGE_DISPLAY_COUNT)*PAGE_DISPLAY_COUNT;
		//하단 끝 페이지 번호
		int endPageNum=startPageNum+PAGE_DISPLAY_COUNT-1;
		
		//전체글의 갯수
		int totalRow = serviceDao.getCount(dto);
		
		//전체 페이지의 갯수 구하기
		int totalPageCount=(int)Math.ceil(totalRow/(double)PAGE_ROW_COUNT);
		//끝 페이지 번호가 이미 전체 페이지 갯수보다 크게 계산되었다면 잘못된 값이다.
		if(endPageNum > totalPageCount){
			endPageNum=totalPageCount; //보정해 준다. 
		}
		// startRowNum 과 endRowNum 을 ProductDto 객체에 담아서
		dto.setStartRowNum(startRowNum);
		dto.setEndRowNum(endRowNum);
		dto.setSortBy(sortBy);
		dto.setDirection(direction);
		
		//글 목록 얻어오기 (검색 키워드가 있다면 조건에 맞는 목록만 얻어낸다)
		List<ServiceDto> list = serviceDao.selectPage(dto);

		return ServiceListResponse.builder()
				.list(list)
				.pageNum(pageNum)
				.startPageNum(startPageNum)
				.endPageNum(endPageNum)
				.totalPageCount(totalPageCount)
				.totalRow(totalRow)
				.build();
	}
	
	@Override
	public void save(ServiceDto dto) {
		ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDateTime ldt = zdt.toLocalDateTime();
		dto.setCreatedAt(ldt);
		serviceDao.insert(dto);
	}

	@Override
	public void modifyService(ServiceDto dto) {
		ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDateTime ldt = zdt.toLocalDateTime();
		dto.setUpdatedAt(ldt);
		int rowCount = serviceDao.update(dto);
		if(rowCount == 0) {
			throw new RuntimeException("상품 수정 실패!");
		}
		
	}

	@Override
	public ServiceDto getDetail(int serviceId) {
		
		return serviceDao.getByNum(serviceId);
	}

	@Override
	public void updateServiceStatus(int serviceId, boolean isActive) {
		// DTO 객체 생성 (MyBatis 파라미터 타입에 맞춤)
        ServiceDto dto = new ServiceDto();
        dto.setServiceId(serviceId); // serviceId 필드가 DTO에 있다고 가정
        dto.setIsActive(isActive);
        
        // DAO를 통해 업데이트 실행
        int updatedRows = serviceDao.updateServiceStatus(dto);
        
        // 업데이트 성공 여부 확인
        if (updatedRows == 0) {
            throw new RuntimeException("상품 상태 업데이트 실패: serviceId " + serviceId + " 를 찾을 수 없습니다.");
        }
		
	}
	
}
