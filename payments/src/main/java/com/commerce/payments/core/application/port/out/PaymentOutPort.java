package com.commerce.payments.core.application.port.out;

import com.commerce.payments.core.domain.aggregate.Payment;
import com.commerce.payments.core.domain.aggregate.PaymentPartCancel;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.PaymentId;

import java.util.Optional;

public interface PaymentOutPort {
    void savePayment(Payment payment);
    Optional<Payment> findByOrderId(OrderId orderId);
    
    // 부분취소 관련
    PaymentPartCancel savePartCancel(PaymentPartCancel partCancel);
    boolean existsPartCancelByPaymentId(PaymentId paymentId);
    Money getRemainAmount(PaymentId paymentId);
}
