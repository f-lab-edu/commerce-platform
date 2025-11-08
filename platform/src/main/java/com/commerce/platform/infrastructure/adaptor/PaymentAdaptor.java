package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.out.PaymentOutPort;
import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.infrastructure.persistence.PaymentPartCancelRepository;
import com.commerce.platform.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentAdaptor implements PaymentOutPort {
    private final PaymentRepository paymentRepository;
//    private final PaymentPartCancelRepository paymentPartCancelRepository;

    @Override
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
