package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.port.in.PaymentService;
import com.commerce.shared.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/approval")
    public ResponseEntity<String> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            paymentService.processApproval(paymentRequest);
        } catch (BusinessException e) {
            return ResponseEntity.ok("실패");
        } catch (Exception e) {
            return ResponseEntity.ok("실패");
        }

        return ResponseEntity.ok("성공");
    }

    @PatchMapping("/cancel")
    public ResponseEntity<String> fullCancel(@Valid @RequestBody PaymentCancelRequest cancelRequest) {
        try {

            paymentService.processCancel(cancelRequest);
        } catch (BusinessException e) {
            return ResponseEntity.ok("실패");
        } catch (Exception e) {
            return ResponseEntity.ok("실패");
        }

        return ResponseEntity.ok("성공");
    }

    @PatchMapping("/partial-cancel")
    public ResponseEntity<String> partialCancel(@Valid @RequestBody PaymentCancelRequest cancelRequest) {
        try {
            paymentService.processPartialCancel(cancelRequest);
        } catch (BusinessException e) {
            return ResponseEntity.ok("실패");
        } catch (Exception e) {
            return ResponseEntity.ok("실패");
        }

        return ResponseEntity.ok("성공");
    }

    @PostMapping("/registry-card/{cardId}")
    public ResponseEntity<String> createPaymentWithRegistryCard(
            @PathVariable Long cardId,
            @RequestBody Map<String, String> body) {

        // todo
//        paymentUseCase.doApprovalWithCardId(cardId);

        return ResponseEntity.ok("성공");
    }
}
