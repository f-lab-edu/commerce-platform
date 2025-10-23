package com.commerce.platform.core.application.out;

import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.OrderStatus;

import java.util.Optional;

public interface OrderOutputPort {
    Order saveOrder(OrderRequest request);
    Optional<Order> findById(String orderId);
    Order updateOrder(String orderId, String reason, OrderStatus status);

}
