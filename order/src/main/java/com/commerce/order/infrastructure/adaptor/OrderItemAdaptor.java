package com.commerce.order.infrastructure.adaptor;

import com.commerce.order.core.application.port.out.OrderItemOutPort;
import com.commerce.order.core.domain.aggregate.OrderItem;
import com.commerce.order.infrastructure.persistence.OrderItemRepository;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderItemAdaptor implements OrderItemOutPort {
    private final OrderItemRepository repository;
    @Override
    public void saveAll(List<OrderItem> orderItems) {
        repository.saveAll(orderItems);
    }

    @Override
    public List<OrderItem> findByOrderId(OrderId orderId) {
        return repository.findByOrderId(orderId);
    }

    @Override
    public Optional<OrderItem> findById(Long orderItemId) {
        return repository.findById(orderItemId);
    }
}
