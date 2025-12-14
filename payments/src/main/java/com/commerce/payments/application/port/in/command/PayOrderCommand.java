package com.commerce.payments.application.port.in.command;

import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.payments.domain.enums.PayProvider;
import com.commerce.payments.domain.enums.PaymentStatus;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class PayOrderCommand {
        private OrderId orderId;
        private Money approvedAmount;
        private int installment;
        private PayMethod payMethod;
        private PayProvider payProvider;
        private final PaymentStatus paymentStatus = PaymentStatus.APPROVED;
        private String jsonSubData;  // pg사 요구 데이터
}
