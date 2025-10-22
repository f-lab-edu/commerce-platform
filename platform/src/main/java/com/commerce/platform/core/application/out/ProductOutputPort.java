package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.Product;

import java.util.List;
import java.util.Optional;

public interface ProductOutputPort {
    List<Product> findAll();
    Optional<Product> findById(String productId);
}
