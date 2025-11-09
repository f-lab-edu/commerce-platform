package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {

    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderId(OrderId orderId);
}
