package com.commerce.platform.core.application.port.in.dto;

import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;

import java.util.List;

public record CreateOrderCommand(
        CustomerId customerId,
        CouponId couponId,
        List<OrderItemCommand> orderItemCommands
) {
    public record OrderItemCommand(
            ProductId productId,
            Quantity quantity
    ) {}

    public static CreateOrderCommand from(OrderRequest orderRequest) {
        List<OrderItemCommand> itemCommands = orderRequest.orderItemRequests().stream()
                .map(itemReq -> {
                    return new OrderItemCommand(
                            ProductId.of(itemReq.productId()),
                            Quantity.create(itemReq.quantity()));
                })
                .toList();

        return new CreateOrderCommand(
                CustomerId.of(orderRequest.customerId()),
                CouponId.of(orderRequest.couponId()),
                itemCommands
        );

    }
}
