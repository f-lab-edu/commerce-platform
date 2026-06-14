package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.InventoryStockPort;
import com.commerce.inventory.core.domain.vo.ItemDeductOutcome;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.OrderAggregateEvent;
import com.commerce.shared.kafka.event.dto.StockCommandEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * B2 상품 단위 재고 처리. type으로 분기해 DB 단일 조건부 UPDATE(SELECT 없음)로 차감/복원한다.
 * 같은 productId가 파티션 키로 단일 스레드에 직렬화되고, 차감은 행 원자성으로 오버셀이 불가능하다.
 *
 * 각 처리는 @Transactional. order-aggregate 발행은 TransactionalEventPublisher가 커밋 후 발행한다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryStockCommandUseCaseImpl implements InventoryStockCommandUseCase {

    private final InventoryStockPort stockPort;
    private final TransactionalEventPublisher eventPublisher;

    @Override
    @Transactional
    public void handle(StockCommandEvent event) {
        ProductId productId = ProductId.of(event.productId());

        switch (event.type()) {
            case DEDUCT -> handleDeduct(event, productId);
            case REPLENISH -> handleReplenish(event, productId);
        }
    }

    private void handleDeduct(StockCommandEvent event, ProductId productId) {
        int affected = stockPort.deductIfEnough(productId, event.quantity());
        ItemDeductOutcome outcome = ItemDeductOutcome.fromAffected(affected);

        if (outcome == ItemDeductOutcome.INSUFFICIENT) {
            log.warn("[Inventory-B2] 재고 부족 - orderId: {}, productId: {}, qty: {}",
                    event.orderId(), event.productId(), event.quantity());
        }

        // 주문 기인(orderId 존재)일 때만 재집계 결과 발행. (관리자 차감 = orderId 없음 → 범위 밖)
        if (StringUtils.hasText(event.orderId())) {
            eventPublisher.publish(EventTopic.INVENTORY_ORDER_AGGREGATE_TOPIC,
                    new OrderAggregateEvent(
                            event.orderId(),
                            event.productId(),
                            event.quantity(),      // 성공 시 복원에 쓸 수량
                            outcome.isSuccess(),
                            event.totalItems(),
                            event.customerId(),    // 컨텍스트 스칼라 그대로 전달
                            event.couponId(),
                            event.payMethod(),
                            event.payProvider(),
                            event.orderId(),       // key = orderId (같은 주문 직렬화)
                            LocalDateTime.now()
                    ));
        }
    }

    private void handleReplenish(StockCommandEvent event, ProductId productId) {
        int affected = stockPort.replenish(productId, event.quantity());
        if (affected == 0) {
            log.warn("[Inventory-B2] 보상 복원 대상 없음(상품 행 없음) - orderId: {}, productId: {}, qty: {}",
                    event.orderId(), event.productId(), event.quantity());
        } else {
            log.info("[Inventory-B2] 보상 복원 - orderId: {}, productId: {}, qty: {}",
                    event.orderId(), event.productId(), event.quantity());
        }
        // REPLENISH는 재집계 신호를 발행하지 않는다(복원은 보상 종착점).
    }
}
