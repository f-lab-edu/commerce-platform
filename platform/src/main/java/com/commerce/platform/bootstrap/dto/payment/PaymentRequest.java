package com.commerce.platform.bootstrap.dto.payment;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import jakarta.validation.constraints.NotBlank;

/**
 * 승인 요청 dto
 */
public record PaymentRequest(
        @NotBlank
        String orderId,

        @NotBlank
        PayMethod payMethod,

        @NotBlank
        PayProvider payProvider,

        @NotBlank
        String installment
) { }
