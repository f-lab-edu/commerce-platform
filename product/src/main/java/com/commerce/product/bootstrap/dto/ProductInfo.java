package com.commerce.product.bootstrap.dto;

import com.commerce.product.core.domain.aggregate.Product;

public record ProductInfo (
        String productId,
        String name,
        long price
) {
    /**
     * domain -> api dto
     */
    public static ProductInfo from(Product product) {
        return new ProductInfo(
                product.getProductId().id(),
                product.getProductName(),
                product.getPrice().value()
        );
    }
}
