package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.out.OrderItemOutPort;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.infrastructure.persistence.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
