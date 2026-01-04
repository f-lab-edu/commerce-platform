package com.commerce.payments.infrastructure.adaptor;

import com.commerce.payments.application.port.out.PaymentOutPort;
import com.commerce.payments.domain.aggregate.Payment;
import com.commerce.payments.domain.aggregate.PaymentPartCancel;
import com.commerce.payments.infrastructure.persistence.PaymentPartCancelRepository;
import com.commerce.payments.infrastructure.persistence.PaymentRepository;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentAdaptor implements PaymentOutPort {
    private final PaymentRepository paymentRepository;
    private final PaymentPartCancelRepository paymentPartCancelRepository;

    @Override
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public PaymentPartCancel savePartCancel(PaymentPartCancel partCancel) {
        return paymentPartCancelRepository.save(partCancel);
    }

    @Override
    public boolean existsPartCancelByPaymentId(PaymentId approvedpaymentId) {
        return paymentPartCancelRepository.existsPaymentPartCancelByApprovedPaymentId(approvedpaymentId);
    }

    @Override
    public Money getRemainAmount(PaymentId paymentId) {
        return paymentPartCancelRepository.selectRemainAmountByPaymentId(paymentId);
    }
}
