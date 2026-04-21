package com.commerce.product.infrastructure.adaptor;

import com.commerce.product.core.application.port.out.ProductOutputPort;
import com.commerce.product.core.domain.aggregate.Product;
import com.commerce.shared.vo.ProductId;
import com.commerce.product.infrastructure.persistence.ProductRepository;
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
