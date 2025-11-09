package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.aggreate.PaymentPartCancel;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.PaymentId;

import java.util.Optional;

public interface PaymentOutPort {
    void savePayment(Payment payment);
    Optional<Payment> findByOrderId(OrderId orderId);
    
    // 부분취소 관련
    void savePartCancel(PaymentPartCancel partCancel);
    boolean existsPartCancelByPaymentId(PaymentId paymentId);
    Money getRemainAmount(PaymentId paymentId);
}
