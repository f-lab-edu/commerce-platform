package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.OrderOutputPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.OrderId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderAdaptor implements OrderOutputPort {
    @Override
    public Order saveOrder(Order order) {
        return null;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return null;
    }
}
