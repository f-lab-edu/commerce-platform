package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.in.PaymentUseCase;
import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.in.dto.PayResult;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.shared.exception.BusinessException;
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

    @PostMapping("/approval")
    public ResponseEntity<PayResult> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        PayResult result = null;

        try {
            PayOrderCommand command = new PayOrderCommand(
                    OrderId.of(paymentRequest.orderId()),
                    null,
                    paymentRequest.installment(),
                    paymentRequest.payMethod(),
                    paymentRequest.payProvider(),
                    null
            );

            paymentUseCase.doApproval(command);
        } catch (BusinessException e) {
            result = new PayResult.Failed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            result = new PayResult.Failed("9999", "승인 처리 중 오류가 발생했습니다");
        }

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/cancel")
    public ResponseEntity<PayResult> fullCancel(@Valid @RequestBody PaymentCancelRequest cancelRequest) {
        PayResult result = null;

        try {
            PayCancelCommand cancelCommand = PayCancelCommand.builder()
                    .orderId(cancelRequest.orderId())
                    .paymentStatus(PaymentStatus.FULL_CANCELED)
                    .build();

            paymentUseCase.doCancel(cancelCommand);
        } catch (BusinessException e) {
            result = new PayResult.Failed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            result = new PayResult.Failed("9999", "전체취소 처리 중 오류가 발생했습니다");
        }

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/partial-cancel")
    public ResponseEntity<PayResult> partialCancel(@Valid @RequestBody PaymentCancelRequest cancelRequest) {
        PayResult result = null;

        try {
            PayCancelCommand cancelCommand = PayCancelCommand.builder()
                    .orderId(cancelRequest.orderId())
                    .orderItemId(cancelRequest.orderItemId())
                    .canceledQuantity(cancelRequest.canceledQuantity())
                    .paymentStatus(PaymentStatus.FULL_CANCELED)
                    .build();

            paymentUseCase.doPartCancel(cancelCommand);
        } catch (BusinessException e) {
            result = new PayResult.Failed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            result = new PayResult.Failed("9999", "부분취소 처리 중 오류가 발생했습니다");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/registry-card/{cardId}")
    public ResponseEntity<String> createPaymentWithRegistryCard(
            @PathVariable Long cardId,
            @RequestBody Map<String, String> body) {

        // todo
        paymentUseCase.doApprovalWithCardId(cardId);

        return ResponseEntity.ok("성공");
    }
}
