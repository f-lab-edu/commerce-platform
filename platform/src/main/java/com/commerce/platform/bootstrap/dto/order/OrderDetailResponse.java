package com.commerce.platform.bootstrap.dto.order;

import com.commerce.platform.core.application.in.dto.OrderView;
import com.commerce.platform.core.application.in.dto.OrderView.OrderItemView;

import java.time.format.DateTimeFormatter;
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
    ){
        public static OrderItemResponse from(OrderItemView orderItemView) {
            return new OrderItemResponse(
                    orderItemView.productId().id(),
                    orderItemView.productName(),
                    orderItemView.amt().value(),
                    orderItemView.quantity().value()
            );
        }
    }

    /** OrderDetailResponse 생성 */
    public static OrderDetailResponse from(OrderView orderView) {
        return new OrderDetailResponse(
                orderView.orderId().id(),
                orderView.orderItemViews().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                orderView.originAmt().value(),
                orderView.discountAmt().value(),
                orderView.resultAmt().value(),
                orderView.orderStatus().getValue(),
                orderView.orderedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm"))
        );
    }
}
