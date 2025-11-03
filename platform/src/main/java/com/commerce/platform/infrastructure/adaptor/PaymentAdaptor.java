package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.out.PaymentOutPort;
import com.commerce.platform.infrastructure.persistence.PaymentPartCancelRepository;
import com.commerce.platform.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentAdaptor implements PaymentOutPort {
    private final PaymentRepository paymentRepository;
    private final PaymentPartCancelRepository paymentPartCancelRepository;
}
