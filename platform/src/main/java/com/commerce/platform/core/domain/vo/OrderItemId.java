package com.commerce.platform.core.domain.vo;

public record OrderItemId(
        OrderId orderId,
        ProductId productId
) {
}
