package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.OrderItemOutPort;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.vo.OrderId;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemAdaptor implements OrderItemOutPort {
    @Override
    public void saveAll(List<OrderItem> orderItems) {

    }

    @Override
    public List<OrderItem> findByOrderId(OrderId orderId) {
        return null;
    }
}
