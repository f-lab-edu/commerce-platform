package com.commerce.product.core.application.port.in;

import com.commerce.product.core.application.port.in.dto.ProductDetail;
import com.commerce.product.core.domain.aggregate.Product;
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
