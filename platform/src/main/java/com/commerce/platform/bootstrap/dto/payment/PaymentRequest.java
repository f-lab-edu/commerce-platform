package com.commerce.platform.bootstrap.dto.payment;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank
        String orderId,

        @Min(value = 0)
        long amount,

        @NotBlank
        PayMethod payMethod,

        @NotBlank
        PaymentStatus paymentStatus,

        String installment
) {
        public PayOrderCommand toApproval() {
                return new PayOrderCommand(
                        OrderId.of(this.orderId),
                        Money.create(amount),
                        null,
                        installment,
                        payMethod,
                        PaymentStatus.APPROVED
                );
        }

        public PayOrderCommand toCancel() {
                return new PayOrderCommand(
                        OrderId.of(this.orderId),
                        null,
                        Money.create(amount),
                        null,
                        payMethod,
                        paymentStatus
                );
        }
}
