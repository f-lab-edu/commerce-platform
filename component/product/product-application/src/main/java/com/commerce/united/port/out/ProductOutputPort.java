package com.commerce.united.port.out;

import domain.aggregate.Product;

import java.util.List;
import java.util.Optional;

public interface ProductOutputPort {
    List<Product> findAll();
    Optional<Product> findById(String productId);
}
