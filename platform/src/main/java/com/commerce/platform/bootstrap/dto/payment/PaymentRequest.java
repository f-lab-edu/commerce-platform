package com.commerce.platform.bootstrap.dto.payment;

import com.commerce.shared.vo.OrderId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 승인 요청 dto
 */
public record PaymentRequest(
        @NotNull
        OrderId orderId,

        @NotBlank
        String payMethod,

        @NotBlank
        String payProvider,

        @Min(0)
        int installment
) { }
