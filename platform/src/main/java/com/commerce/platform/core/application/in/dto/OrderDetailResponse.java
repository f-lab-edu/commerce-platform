package com.commerce.platform.core.application.in.dto;

import java.util.List;

public record OrderDetailResponse(
        String orderId,
        List<OrderItemResponse> orderItems,
        long originAmt,
        long discountAmt,
        long resultAmt,
        String orderStatus,
        String orderedDateTime
) {
    /** 주문상품 상세 */
    public record OrderItemResponse(
            String productId,
            String productName,
            long amt,
            long quantity
    ){}
}
