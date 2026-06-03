package com.commerce.order.infrastructure.adaptor;

import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.aggregate.OrderItem;
import com.commerce.order.infrastructure.persistence.OrderItemRepository;
import com.commerce.order.infrastructure.persistence.OrderRepository;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderAdaptor implements OrderOutputPort {
    private final OrderRepository repository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Order 애그리거트를 atomically 저장한다. createOrder 흐름에서는 items가 채워져 있고,
     * 그 외(상태 전이 후 저장 등)에는 비어있어 Order row만 갱신된다.
     */
    @Override
    public Order saveOrder(Order order) {
        Order saved = repository.save(order);
        List<OrderItem> items = order.getItems();
        if (!items.isEmpty()) {
            orderItemRepository.saveAll(items);
        }
        return saved;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return repository.findById(orderId);
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> findPendingOrdersBefore(LocalDateTime threshold) {
        return repository.findByStatusPendingAndOrderedAtBefore(threshold);
    }
}
