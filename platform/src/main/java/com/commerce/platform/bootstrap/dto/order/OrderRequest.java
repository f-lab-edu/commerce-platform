package com.commerce.platform.bootstrap.dto.order;

import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.vo.*;

import java.util.List;

public record OrderRequest(
        String productId,
        String customerId,
        String couponId,
        int quantity
){
    public static Order to(OrderRequest request) {
        return Order.create(
                new CustomerId(request.customerId),
                CouponId.of(request.couponId),
                List.of(
                        OrderItem.create(ProductId.of(request.productId),
                                Money.create(0),
                                Quantity.create(request.quantity)))
        );
    }
}
