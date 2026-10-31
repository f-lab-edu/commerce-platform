package com.commerce.order.core.application.port.in.dto;

import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;

import java.util.List;

/**
 * 결제 완료 후 주문 확정 커맨드.
 * 금액 반영 + order.confirmed 발행에 필요한 saga 컨텍스트(items, 결제조건, paymentId)를 함께 전달한다.
 */
public record CompleteOrderCommand(
        OrderId orderId,
        Money originAmt,
        Money discountAmt,
        String paymentId,
        List<ItemEntry> items,
        String payMethod,
        String payProvider,
        int installment
) { }
