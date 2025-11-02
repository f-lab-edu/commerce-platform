package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, OrderId> {

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId")
    List<Order> findByCustomerId(CustomerId customerId);
}
