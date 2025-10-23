package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.ProductOutputPort;
import com.commerce.platform.core.domain.aggreate.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductPersistenceAdaptor implements ProductOutputPort {
    @Override
    public List<Product> findAll() {
        return null;
    }

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.empty();
    }
}
