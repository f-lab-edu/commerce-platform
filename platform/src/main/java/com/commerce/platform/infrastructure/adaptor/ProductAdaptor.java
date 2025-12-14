package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.port.out.ProductOutputPort;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.infrastructure.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductAdaptor implements ProductOutputPort {
    private final ProductRepository repository;

    @Override
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return repository.findById(productId);
    }

    @Override
    public void save(Product product) {
        repository.save(product);
    }

    @Override
    public List<Product> findByIdIn(List<ProductId> productIds) {
        return repository.findAllById(productIds);
    }
}
