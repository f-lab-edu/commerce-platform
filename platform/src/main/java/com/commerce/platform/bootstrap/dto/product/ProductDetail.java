package com.commerce.platform.bootstrap.dto.product;

import com.commerce.platform.core.domain.aggreate.Product;

public record ProductDetail(
        String productId,
        String name,
        long price,
        String description,
        String productStatus
) {
    public static ProductDetail from(Product product) {
        return new ProductDetail(
                product.getProductId().id(),
                product.getProductName(),
                product.getPrice().value(),
                product.getDescription(),
                product.getStatus().getDescription()
        );
    }
}
