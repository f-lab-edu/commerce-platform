package com.commerce.order.core.application.port.out;

import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.shared.kafka.event.dto.ItemEntry;

import java.util.List;

/**
 * 주문 saga 이벤트 발행 포트. 구현체는 트랜잭션 커밋 후 발행을 보장한다.
 */
public interface OrderEventPort {

    /** 주문 생성(order.created). items는 생성 직후 Order가 보유한 값을 사용한다. */
    void publishOrderCreated(Order order, String payMethod, String payProvider, int installment);

    /** 주문 확정(order.confirmed). JPA로 로드한 Order는 items가 비어있어 saga 컨텍스트의 items를 받는다. */
    void publishOrderConfirmed(Order order, List<ItemEntry> items,
                               String payMethod, String payProvider, int installment, String paymentId);
}
