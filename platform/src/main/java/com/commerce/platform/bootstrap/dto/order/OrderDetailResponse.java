package com.commerce.platform.bootstrap.dto.order;

import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;

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
        public static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getProductId().id(),
                    orderItem.getProductName(),
                    orderItem.getAmt().value(),
                    orderItem.getQuantity().value()
            );
        }
    }

    /** OrderDetailResponse 생성 */
    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getOrderId().id(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getOriginAmt().value(),
                order.getDiscountAmt().value(),
                order.getResultAmt().value(),
                order.getStatus().getValue(),
                order.getOrderedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm"))
        );
    }
}
