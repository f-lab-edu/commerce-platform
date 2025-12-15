package com.commerce.platform.bootstrap.dto.payment;

import com.commerce.shared.vo.OrderId;
import jakarta.validation.constraints.NotBlank;

/**
 * 승인 요청 dto
 */
public record PaymentRequest(
        @NotBlank
        OrderId orderId,

        @NotBlank
        String payMethod,

        @NotBlank
        String payProvider,

        @NotBlank
        int installment
) { }
