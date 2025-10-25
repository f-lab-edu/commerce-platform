package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.product.ProductInfo;
import com.commerce.platform.core.application.vo.UpdateStockCommand;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;

import java.util.List;

/**
 *  INBOUND PORT
 */
public interface ProductUseCase {
    List<ProductInfo> getProductList(int page);
    ProductInfo getProduct(ProductId productId) throws Exception;
    void createProduct(Product product);
    Product updateStock(UpdateStockCommand stockCommand) throws Exception;
}
