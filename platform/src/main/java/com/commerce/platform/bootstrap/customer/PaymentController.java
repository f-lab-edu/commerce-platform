package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.port.in.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RequestMapping("/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/approval")
    public CompletableFuture<ResponseEntity<String>> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        return paymentService.processApproval(paymentRequest)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    return ResponseEntity.ok("실패");
                });
    }

    @PatchMapping("/cancel")
    public CompletableFuture<ResponseEntity<String>> fullCancel(@Valid @RequestBody PaymentCancelRequest cancelRequest) {
        return paymentService.processCancel(cancelRequest)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    return ResponseEntity.ok("실패");
                });
    }

    @PatchMapping("/partial-cancel")
    public CompletableFuture<ResponseEntity<String>> partialCancel(@Valid @RequestBody PaymentCancelRequest cancelRequest) {
        return paymentService.processPartialCancel(cancelRequest)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    return ResponseEntity.ok("실패");
                });
    }

 /*   @PostMapping("/registry-card/{cardId}")
    public CompletableFuture<ResponseEntity<String>> createPaymentWithRegistryCard(
            @PathVariable Long cardId,
            @RequestBody Map<String, String> body) {
                return paymentService.doApprovalWithCardId(cardId);

    }*/
}
