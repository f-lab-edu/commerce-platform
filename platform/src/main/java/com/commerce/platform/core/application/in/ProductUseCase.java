package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.product.ProductInfo;
import java.util.List;

/**
 *  INBOUND PORT
 */
public interface ProductUseCase {
    List<ProductInfo> getProductList();
    ProductInfo getProduct(String productId) throws Exception;
}
