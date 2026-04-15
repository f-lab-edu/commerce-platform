package com.commerce.order.infrastructure.adaptor;

import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.infrastructure.persistence.OrderRepository;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderAdaptor implements OrderOutputPort {
    private final OrderRepository repository;
    @Override
    public Order saveOrder(Order order) {
        return repository.save(order);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return repository.findById(orderId);
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return repository.findByCustomerId(customerId);
    }
}
