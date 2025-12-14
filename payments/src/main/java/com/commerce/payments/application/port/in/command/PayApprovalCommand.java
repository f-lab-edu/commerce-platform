package com.commerce.payments.application.port.in.command;

import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.payments.domain.enums.PayProvider;
import com.commerce.payments.domain.enums.PgProvider;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayApprovalCommand {
    private final OrderId orderId;
    private final Money approvedAmount;
    private final String installment;
    private final PayMethod payMethod;
    private final PayProvider payProvider;
    private final PgProvider pgProvider;
}
