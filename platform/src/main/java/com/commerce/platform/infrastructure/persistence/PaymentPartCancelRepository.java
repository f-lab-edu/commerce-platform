package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.PaymentPartCancel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentPartCancelRepository extends JpaRepository<PaymentPartCancel, Long> {
}
