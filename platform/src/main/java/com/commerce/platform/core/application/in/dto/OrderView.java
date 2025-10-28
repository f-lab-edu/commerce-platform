package com.commerce.platform.core.application.in.dto;

import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.enums.OrderStatus;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;

import java.time.LocalDateTime;
import java.util.List;

public record OrderView(
        OrderId orderId,
        List<OrderItemView> orderItemViews,
        Money originAmt,
        Money discountAmt,
        Money resultAmt,
        OrderStatus orderStatus,
        LocalDateTime orderedDateTime
) {

    public static OrderView from(Order order) {
        return new OrderView(
                order.getOrderId(),
                order.getOrderItems().stream()
                        .map(OrderItemView::from)
                        .toList(),
                order.getOriginAmt(),
                order.getDiscountAmt(),
                order.getResultAmt(),
                order.getStatus(),
                order.getOrderedAt()
        );
    }

    /** 주문상품 상세 */
    public record OrderItemView(
            ProductId productId,
            String productName,
            Money amt,
            Quantity quantity
    ){
        public static OrderItemView from(OrderItem orderItem) {
            return new OrderItemView(
                    orderItem.getProductId(),
                    orderItem.getProductName(),
                    orderItem.getAmt(),
                    orderItem.getQuantity()
            );
        }
    }
}
