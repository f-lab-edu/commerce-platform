package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.platform.core.application.out.OrderOutputPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderPersistenceAdaptor implements OrderOutputPort {
    @Override
    public Order saveOrder(OrderRequest request) {
        return null;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.empty();
    }

    @Override
    public Order updateOrder(String orderId, String reason, OrderStatus status) {
        return null;
    }
}
