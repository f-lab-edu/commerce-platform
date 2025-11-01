package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class OrderItem {
    private OrderId orderId;
    private ProductId productId;
    private Quantity quantity;

    public static OrderItem create(
            OrderId orderId,
            ProductId productId,

            Quantity quantity
    ) {
       return OrderItem.builder()
               .orderId(orderId)
               .productId(productId)
               .quantity(quantity)
               .build();
    }
}
