package com.commerce.platform.core.application.port.in;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;

import java.util.concurrent.CompletableFuture;

public interface PaymentService {
    CompletableFuture<String> processApproval(PaymentRequest request);
    CompletableFuture<String> processCancel(PaymentCancelRequest request);
    CompletableFuture<String> processPartialCancel(PaymentCancelRequest request);
}
