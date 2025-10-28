package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.OrderId;

import java.util.List;
import java.util.Optional;

public interface OrderOutputPort {
    Order saveOrder(Order order);
    Optional<Order> findById(OrderId orderId);
    List<Order> findByCustomerId(CustomerId customerId);
}
