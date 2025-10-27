package com.commerce.platform.bootstrap.dto.order;

import java.util.List;

public record OrderRequest(
        String customerId,
        String couponId,
        List<OrderItemRequest> orderItemRequests
){
    public record OrderItemRequest(
            String productId,
            int quantity
    ) {}
}
