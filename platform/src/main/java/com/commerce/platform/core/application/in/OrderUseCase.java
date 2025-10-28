package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.order.OrderRefundRequest;
import com.commerce.platform.core.application.in.dto.CreateOrderCommand;
import com.commerce.platform.core.application.in.dto.OrderView;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.OrderId;

import java.util.List;

public interface OrderUseCase {
    Order createOrder(CreateOrderCommand orderCommand);
    List<Order> getOrders(CustomerId customerId);
    OrderView getOrder(OrderId orderId);
    Order cancelOrder(OrderId orderId, String reason);
    Order refundOrder(OrderId orderId, OrderRefundRequest request);
}
