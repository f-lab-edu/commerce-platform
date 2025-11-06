package com.commerce.platform.bootstrap.dto.product;


import com.commerce.platform.core.domain.aggreate.Product;

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
