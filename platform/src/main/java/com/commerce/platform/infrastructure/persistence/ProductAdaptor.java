package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.ProductOutputPort;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductAdaptor implements ProductOutputPort {
    @Override
    public List<Product> findAll() {
        return null;
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return Optional.empty();
    }

    @Override
    public void save(Product product) {
    }

    @Override
    public List<Product> findByIdIn(List<ProductId> productIds) {
        return null;
    }
}
