package com.commerce.payments.infrastructure.persistence;

import com.commerce.payments.core.domain.aggregate.PaymentFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentFailureRepository extends JpaRepository<PaymentFailure, Long> {
}
