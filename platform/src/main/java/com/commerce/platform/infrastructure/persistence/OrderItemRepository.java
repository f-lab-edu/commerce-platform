package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId")
    List<OrderItem> findByOrderId(OrderId orderId);

    Optional<OrderItem> findByOrderIdAndProductIdAndCanceled(OrderId orderId, ProductId productId, boolean b);
}
