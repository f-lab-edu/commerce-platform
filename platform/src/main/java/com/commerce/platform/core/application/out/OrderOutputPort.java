package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.enums.OrderStatus;
import com.commerce.platform.core.domain.vo.OrderId;

import java.util.Optional;

public interface OrderOutputPort {
    Order saveOrder(Order order);
    Optional<Order> findById(OrderId orderId);
    Order updateOrder(String orderId, String reason, OrderStatus status);

}
