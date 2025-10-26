package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.vo.UpdateStockCommand;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;

import java.util.List;

/**
 *  INBOUND PORT
 */
public interface ProductUseCase {
    List<Product> getProductList(int page);
    Product getProduct(ProductId productId);
    ProductId createProduct(Product product);
    Product updateStock(UpdateStockCommand stockCommand);
}
