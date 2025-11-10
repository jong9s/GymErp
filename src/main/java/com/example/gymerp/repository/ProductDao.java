package com.example.gymerp.repository;

import java.util.List;

import com.example.gymerp.dto.ProductDto;

public interface ProductDao {

	public List<ProductDto> selectPage(ProductDto dto);
	public List<ProductDto> selectPageWithoutQuantity(ProductDto dto);
	public int getCount(ProductDto dto);

	public void insert(ProductDto dto);
	public int update(ProductDto dto);
	public ProductDto getByNum(int productId);
	public int updateProductStatus(ProductDto dto);
}
