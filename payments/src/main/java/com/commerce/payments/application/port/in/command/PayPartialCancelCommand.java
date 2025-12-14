package com.commerce.payments.application.port.in.command;

import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayPartialCancelCommand {
    private final OrderId orderId;
    private final Long orderItemId;
    private final Integer canceledQuantity;
    private final Money canceledAmount;
}
