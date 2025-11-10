package com.example.gymerp.service;

import com.example.gymerp.dto.ProductDto;
import com.example.gymerp.dto.ProductListResponse;
import com.example.gymerp.dto.StockAdjustRequestDto;

public interface ProductService {

	public ProductListResponse getProducts(int pageNum, ProductDto dto, String sortBy, String direction);
	public ProductListResponse getProductsWithoutQuantity(int pageNum, ProductDto dto, String sortBy, String direction);

	public void save(ProductDto dto, StockAdjustRequestDto request);
	public void modifyProduct(ProductDto dto);
	public ProductDto getDetail(int productId);
	public void updateProductStatus(int productId, boolean isActive);
}
