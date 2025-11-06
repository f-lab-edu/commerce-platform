package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderItemId.orderId = :orderId")
    List<OrderItem> findByOrderId(OrderId orderId);
}
