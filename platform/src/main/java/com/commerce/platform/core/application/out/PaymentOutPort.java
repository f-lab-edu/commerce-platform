package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.vo.OrderId;

import java.util.Optional;

public interface PaymentOutPort {
    void savePayment(Payment payment);
    Optional<Payment> findByOrderId(OrderId orderId);
}
