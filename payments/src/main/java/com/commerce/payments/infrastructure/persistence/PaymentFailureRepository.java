package com.commerce.payments.infrastructure.persistence;

import com.commerce.payments.core.domain.aggregate.PaymentFailure;
import com.commerce.shared.vo.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentFailureRepository extends JpaRepository<PaymentFailure, Long> {
    boolean existsByOrderId(OrderId orderId);
}
