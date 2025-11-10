package com.example.gymerp.service;

import java.util.List;
import java.util.UUID;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.gymerp.config.FileStorageProperties;
import com.example.gymerp.dto.ProductDto;
import com.example.gymerp.dto.ProductListResponse;
import com.example.gymerp.dto.StockAdjustRequestDto;
import com.example.gymerp.repository.ProductDao;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

	private final ProductDao productDao;
	private final StockService stockService;
	private final FileStorageProperties fileStorageProperties;

	@Override
	public ProductListResponse getProducts(int pageNum, ProductDto dto, String sortBy, String direction) {
		
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
		int totalRow = productDao.getCount(dto);
		
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
		
		//상품 목록 얻어오기 (검색 키워드가 있다면 조건에 맞는 목록만 얻어낸다)
		List<ProductDto> list = productDao.selectPage(dto);

		return ProductListResponse.builder()
				.list(list)
				.pageNum(pageNum)
				.startPageNum(startPageNum)
				.endPageNum(endPageNum)
				.totalPageCount(totalPageCount)
				.totalRow(totalRow)
				.build();
	}
	
	@Override
	public ProductListResponse getProductsWithoutQuantity(int pageNum, ProductDto dto, String sortBy, String direction) {
		
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
		int totalRow = productDao.getCount(dto);
		
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
		
		//상품 목록 얻어오기 (검색 키워드가 있다면 조건에 맞는 목록만 얻어낸다)
		List<ProductDto> list = productDao.selectPageWithoutQuantity(dto);

		return ProductListResponse.builder()
				.list(list)
				.pageNum(pageNum)
				.startPageNum(startPageNum)
				.endPageNum(endPageNum)
				.totalPageCount(totalPageCount)
				.totalRow(totalRow)
				.build();
	}

	@Override
	@Transactional
	public void save(ProductDto dto, StockAdjustRequestDto request) {
		
		//업로드된 이미지가 있는지 읽어와본다
		MultipartFile image = dto.getProfileFile();
		
		//만일 업로드된 이미지가 있다면
		if(image != null && !image.isEmpty()) {
			//원본 파일명
			String orgFileName = image.getOriginalFilename();
			//이미지의 확장자를 유지하기 위해 뒤에 원본 파일명을 추가한다
			String saveFileName = UUID.randomUUID().toString()+orgFileName;
			//저장할 파일의 전체 경로 구성하기
			Path targetPath = fileStorageProperties.prepareUploadDir().resolve(saveFileName);
			try {
				image.transferTo(targetPath.toFile());
			}catch(Exception e) {
				e.printStackTrace();
			}
			//UserDto 에 저장된 이미지의 이름을 넣어준다
			dto.setProfileImage(saveFileName);
		}
		ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDateTime ldt = zdt.toLocalDateTime();
		dto.setCreatedAt(ldt);
		
		productDao.insert(dto);
		
		//리액트에 등록 시 수량 기본값 0 넣기
		if(dto.getQuantity() != 0) {
			request.setDate(dto.getCreatedAt());
			stockService.adjustProduct(dto.getProductId(), request);
		}
		
	}

	@Override
	@Transactional
	public void modifyProduct(ProductDto dto) {
		
		//업로드된 이미지가 있는지 읽어와본다
		MultipartFile image = dto.getProfileFile();
		
		//만일 업로드된 이미지가 있다면
		if(image != null && !image.isEmpty()) {
			//원본 파일명
			String orgFileName = image.getOriginalFilename();
			//이미지의 확장자를 유지하기 위해 뒤에 원본 파일명을 추가한다
			String saveFileName = UUID.randomUUID().toString()+orgFileName;
			//저장할 파일의 전체 경로 구성하기
			Path targetPath = fileStorageProperties.prepareUploadDir().resolve(saveFileName);
			try {
				image.transferTo(targetPath.toFile());
			}catch(Exception e) {
				e.printStackTrace();
			}
			//UserDto 에 저장된 이미지의 이름을 넣어준다
			dto.setProfileImage(saveFileName);
		}
		ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDateTime ldt = zdt.toLocalDateTime();
		dto.setUpdatedAt(ldt);
		
		int rowCount = productDao.update(dto);
		if(rowCount == 0) {
			throw new RuntimeException("상품 수정 실패!");
		}
		
	}

	@Override
	public ProductDto getDetail(int productId) {
		
		return productDao.getByNum(productId);
	}

	@Override
	@Transactional
	public void updateProductStatus(int productId, boolean isActive) {
		// DTO 객체 생성 (MyBatis 파라미터 타입에 맞춤)
        ProductDto dto = new ProductDto();
        dto.setProductId(productId); // productId 필드가 DTO에 있다고 가정
        dto.setIsActive(isActive);
        
        // DAO를 통해 업데이트 실행
        int updatedRows = productDao.updateProductStatus(dto);
        
        // 업데이트 성공 여부 확인
        if (updatedRows == 0) {
            throw new RuntimeException("상품 상태 업데이트 실패: productId " + productId + " 를 찾을 수 없습니다.");
        }
	}
	
	
}
