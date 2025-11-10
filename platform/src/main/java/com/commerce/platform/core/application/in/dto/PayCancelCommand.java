package com.commerce.platform.core.application.in.dto;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.Quantity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 전체/부분 취소 처리 객체
 */
@Setter
@Getter
@Builder
public class PayCancelCommand {
    private OrderId orderId;
    private Long orderItemId;   // 취소할 orderItem
    private Quantity canceledQuantity; // 해당 orderItem의 취소 개수
    private PaymentStatus paymentStatus;

    // 이후 계산 및 db데이터 기반으로 세팅됨
    private Money canceledAmount;
    private PayMethod payMethod;
    private PayProvider payProvider;
    private PgProvider pgProvider;
}
