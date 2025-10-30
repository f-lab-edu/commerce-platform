package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.vo.OrderId;

import java.util.List;

public interface OrderItemOutPort {
    void saveAll(List<OrderItem> orderItems);
    List<OrderItem> findByOrderId(OrderId orderId);
}
