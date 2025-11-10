package com.example.gymerp.repository;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.example.gymerp.dto.ProductDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
@Primary
public class ProductDaoImpl implements ProductDao{
	
	//의존 객체
	private final SqlSession session;

	@Override
	public List<ProductDto> selectPage(ProductDto dto) {
		
		return session.selectList("product.selectPage", dto);
	}
	
	@Override
	public List<ProductDto> selectPageWithoutQuantity(ProductDto dto) {
		
		return session.selectList("product.selectPageWithoutQuantity", dto);
	}

	@Override
	public int getCount(ProductDto dto) {
		
		return session.selectOne("product.getCount", dto);
	}

	@Override
	public void insert(ProductDto dto) {
		session.insert("product.insert", dto);
		
	}

	@Override
	public int update(ProductDto dto) {
		
		return session.update("product.update", dto);
	}

	@Override
	public ProductDto getByNum(int productId) {
		
		return session.selectOne("product.getByNum", productId);
	}

	@Override
	public int updateProductStatus(ProductDto dto) {
		
		return session.update("product.updateStatus", dto);
	}	
}
