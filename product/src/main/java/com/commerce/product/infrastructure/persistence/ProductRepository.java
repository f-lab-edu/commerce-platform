package com.commerce.product.infrastructure.persistence;

import com.commerce.product.core.domain.aggregate.Product;
import com.commerce.shared.vo.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, ProductId> {
}
