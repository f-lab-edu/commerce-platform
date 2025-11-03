package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.in.PaymentUseCase;
import com.commerce.platform.core.application.in.dto.PayResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/payments")
@RestController
public class PaymentController {
    private final PaymentUseCase paymentUseCase;

    @PostMapping
    public ResponseEntity<PayResult> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        PayResult payResult = switch (paymentRequest.paymentStatus()) {
            case APPROVED         -> paymentUseCase.doApproval(paymentRequest.toApproval());
            case FULL_CANCELED    -> paymentUseCase.doCancel(paymentRequest.toCancel());
            case PARTIAL_CANCELED -> paymentUseCase.doPartCancel(paymentRequest.toCancel());
            default -> throw new IllegalStateException("Unexpected value: " + paymentRequest.paymentStatus());
        };

        return ResponseEntity.ok(payResult);
    }
}
