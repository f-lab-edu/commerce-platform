package com.commerce.platform.core.application.port.out;

import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;

import java.util.List;
import java.util.Optional;

public interface ProductOutputPort {
    List<Product> findAll();
    Optional<Product> findById(ProductId productId);
    void save(Product product);
    List<Product> findByIdIn(List<ProductId> productIds);
}
