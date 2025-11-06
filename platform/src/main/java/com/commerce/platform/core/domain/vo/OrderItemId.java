package com.commerce.platform.core.domain.vo;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;

@Embeddable
public record OrderItemId(
        @Embedded
        @AttributeOverride(name = "id", column = @Column(name = "order_id", nullable = false, length = 21))
        OrderId orderId,

        @Embedded
        @AttributeOverride(name = "id", column = @Column(name = "product_id", nullable = false, length = 21))
        ProductId productId
) implements Serializable {
}
