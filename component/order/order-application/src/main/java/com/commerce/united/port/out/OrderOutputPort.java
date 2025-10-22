package com.commerce.united.port.out;

import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.domain.aggregate.Order;
import com.commerce.united.domain.vo.OrderStatus;

import java.util.Optional;

public interface OrderOutputPort {
    Order saveOrder(OrderRequest request);
    Optional<Order> findById(String orderId);
    Order updateOrder(String orderId, String reason, OrderStatus status);

}
