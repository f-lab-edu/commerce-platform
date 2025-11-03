package com.commerce.platform.core.application.in.dto;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;

public record PayOrderCommand(
        OrderId orderId,
        Money approvalAmount,
        Money cancelAmount,
        String installment,
        PayMethod payMethod,
        PaymentStatus paymentStatus
) {
    public PayOrderCommand {
        // PaymentStatus 에 따른 유효성 검증
        if(PaymentStatus.APPROVED == paymentStatus
                && (approvalAmount == null || cancelAmount != null)) {
            throw new IllegalArgumentException("파라미터 확인");
        }

        if((PaymentStatus.FULL_CANCELED == paymentStatus
                || PaymentStatus.PARTIAL_CANCELED == paymentStatus)
                && (approvalAmount != null || cancelAmount == null || cancelAmount.value() < 1)) {
            throw new IllegalArgumentException("파라미터 확인");
        }
    }
}
