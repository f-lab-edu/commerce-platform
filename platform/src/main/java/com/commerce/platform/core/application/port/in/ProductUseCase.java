package com.commerce.platform.core.application.port.in;

import com.commerce.platform.core.application.port.in.dto.ProductDetail;
import com.commerce.platform.core.application.port.in.dto.UpdateStockCommand;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.shared.vo.Quantity;

import java.util.List;

/**
 *  INBOUND PORT
 */
public interface ProductUseCase {
    List<Product> getProductList(int page);
    ProductDetail getProduct(ProductId productId);
    ProductId createProduct(Product product);
    Quantity updateStock(UpdateStockCommand stockCommand);
}
