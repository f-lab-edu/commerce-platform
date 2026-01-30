package com.commerce.payments.infrastructure.persistence;

import com.commerce.payments.core.domain.aggregate.Payment;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {

    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderId(OrderId orderId);
}
