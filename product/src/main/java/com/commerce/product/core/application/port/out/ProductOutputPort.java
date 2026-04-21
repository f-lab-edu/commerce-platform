package com.commerce.product.core.application.port.out;

import com.commerce.product.core.domain.aggregate.Product;
import com.commerce.shared.vo.ProductId;

import java.util.List;
import java.util.Optional;

public interface ProductOutputPort {
    List<Product> findAll();
    Optional<Product> findById(ProductId productId);
    void save(Product product);
    List<Product> findByIdIn(List<ProductId> productIds);
}
