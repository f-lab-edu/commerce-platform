package com.commerce.united.port.in;

import com.commerce.united.port.in.dto.ProductInfo;

import java.util.List;

/**
 *  INBOUND PORT
 */
public interface ProductUseCase {
    List<ProductInfo> getProductList();

    ProductInfo getProduct(String productId) throws Exception;
}
