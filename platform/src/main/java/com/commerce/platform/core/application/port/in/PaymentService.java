package com.commerce.platform.core.application.port.in;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;

public interface PaymentService {
    void processApproval(PaymentRequest request);
    void processCancel(PaymentCancelRequest request);
    void processPartialCancel(PaymentCancelRequest request);
}
