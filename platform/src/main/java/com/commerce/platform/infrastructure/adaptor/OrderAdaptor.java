package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.port.out.OrderOutputPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.shared.vo.CustomerId;
import com.commerce.platform.infrastructure.persistence.OrderRepository;
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
