package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.out.dto.ProductView;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductOutputPort {
    List<Product> findAll();
    Optional<Product> findById(ProductId productId);
    void save(Product product);
    Map<ProductId, ProductView> getOrderItemsByProductId(List<ProductId> productIds); // todo 위치 확인
}
