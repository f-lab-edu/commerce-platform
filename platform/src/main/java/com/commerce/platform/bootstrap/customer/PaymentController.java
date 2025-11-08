package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.in.PaymentUseCase;
import com.commerce.platform.core.application.in.dto.PayResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/payments")
@RestController
public class PaymentController {
    private final PaymentUseCase paymentUseCase;

    /**
     * 카드, 휴대폰, 간편결제의
     * 승인, 취소(부분취소 포함) 처리
     * @param paymentRequest
     * @return
     */
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

    @PostMapping("/registry-card/{cardId}")
    public ResponseEntity<String> createPaymentWithRegistryCard(
            @PathVariable Long cardId,
            @RequestBody Map<String, String> body) {

        paymentUseCase.doApprovalWithCardId(cardId);

        return ResponseEntity.ok("성공");
    }
}
