package com.commerce.product.core.application.port.in.dto;

import com.commerce.product.core.domain.aggregate.Product;

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
