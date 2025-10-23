package com.commerce.platform.bootstrap.dto.product;


import com.commerce.platform.core.domain.aggreate.Product;

public record ProductInfo (
        String productId,
        String name,
        Long price
) {
    /**
     * domain -> api dto
     */
    public static ProductInfo from(Product product) {
        return new ProductInfo(
                product.getProductId(),
                product.getProductName(),
                product.getPrice().getValue()
        );
    }
}
