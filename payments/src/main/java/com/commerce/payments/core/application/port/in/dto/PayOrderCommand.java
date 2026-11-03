package com.commerce.payments.core.application.port.in.dto;

import com.commerce.payments.core.domain.enums.PayMethod;
import com.commerce.shared.enums.PayProvider;
import com.commerce.payments.core.domain.enums.PaymentStatus;
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

        /**
         * 주문 요청값(문자열) 기반 생성
         * payMethod: PayMethod명 (예: CARD), payProvider: PayProvider value (예: shinHan)
         */
        public static PayOrderCommand of(String orderId, long approvedAmount,
                                         String payMethod, String payProvider, int installment) {
                return PayOrderCommand.builder()
                        .orderId(OrderId.of(orderId))
                        .approvedAmount(Money.of(approvedAmount))
                        .payMethod(PayMethod.valueOf(payMethod.toUpperCase()))
                        .payProvider(PayProvider.getPayProviderByValue(payProvider))
                        .installment(installment)
                        .build();
        }
}
