package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.vo.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {
}
