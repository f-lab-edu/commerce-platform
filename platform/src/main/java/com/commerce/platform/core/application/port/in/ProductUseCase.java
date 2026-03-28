package com.commerce.platform.core.application.port.in;

import com.commerce.platform.core.application.port.in.dto.ProductDetail;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.shared.vo.ProductId;

import java.util.List;

/**
 *  INBOUND PORT
 */
public interface ProductUseCase {
    List<Product> getProductList(int page);
    ProductDetail getProduct(ProductId productId);
    ProductId createProduct(Product product);
}