package com.commerce.payments.application.port.in.dto;

import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.payments.domain.enums.PayProvider;
import com.commerce.payments.domain.enums.PaymentStatus;
import com.commerce.payments.domain.enums.PgProvider;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.Quantity;
import lombok.*;

/**
 * 전체/부분 취소 처리 객체
 */
@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PayCancelCommand {
    private OrderId orderId;
    private Long orderItemId;   // 취소할 orderItem
    private Quantity canceledQuantity; // 해당 orderItem의 취소 개수
    private PaymentStatus paymentStatus;
    private String cancelReason;

    // 이후 계산 및 db데이터 기반으로 세팅됨]
    private String pgTid; // pg 승인Tid
    private Money canceledAmount;
    private PayMethod payMethod;
    private PayProvider payProvider;
    private PgProvider pgProvider;

    private RefundReceiveAccount refundReceiveAccount;

    /**
     * 환불 계좌 정보 (가상계좌 전용)
     */
    @Getter
    @AllArgsConstructor
    public class RefundReceiveAccount {
        private String bankCode;
        private String accountNumber;
        private String holderName;
    }
}
