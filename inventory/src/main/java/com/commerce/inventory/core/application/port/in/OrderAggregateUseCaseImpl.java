package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.OrderAggregateStore;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderAggregateEvent;
import com.commerce.shared.kafka.event.dto.StockCommandEvent;
import com.commerce.shared.kafka.event.dto.StockCommandType;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * B2 재집계 finalize. 같은 orderId가 Kafka 파티션 키로 단일 스레드에 직렬화되는 것을 전제로
 * Redis HASH(HSET/HLEN/HGETALL/DEL)만 사용한다(무Lua).
 *
 * record가 반환한 HLEN == totalItems가 되는 도착이 유일 finalizer다. HGETALL로 성공/실패를 분리해
 * 실패 0건이면 inventory.deducted(items는 성공 HASH에서 재구성)를, 실패 ≥1건이면
 * inventory.deduct-failed + 성공분 REPLENISH 보상 fan-out을 발행한다. 컨텍스트 스칼라는
 * 트리거된 aggregate 이벤트 페이로드에서 가져온다(별도 저장 없음).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderAggregateUseCaseImpl implements OrderAggregateUseCase {

    private static final String FAIL_MARK = "F";

    private final OrderAggregateStore store;
    private final TransactionalEventPublisher eventPublisher;

    @Override
    public void handle(OrderAggregateEvent event) {
        String orderId = event.orderId();
        ProductId productId = ProductId.of(event.productId());

        long received = store.record(orderId, productId, event.success(), event.quantity());

        // 정확히 totalItems에 도달한 도착만 finalizer. (미만=미완료, 초과=비정상 재유입 → 이미 finalize·정리됨, skip)
        if (received != event.totalItems()) {
            return;
        }
        // received == totalItems : 유일 finalizer (HSET field 멱등으로 HLEN이 정확히 한 번 임계 도달)
        finalizeOrder(event);
    }

    private void finalizeOrder(OrderAggregateEvent event) {
        String orderId = event.orderId();
        Map<String, String> hash = store.getAll(orderId);

        List<ItemEntry> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (Map.Entry<String, String> e : hash.entrySet()) {
            if (FAIL_MARK.equals(e.getValue())) {
                failed.add(e.getKey());
            } else {
                succeeded.add(new ItemEntry(ProductId.of(e.getKey()), Quantity.create(parseQuantity(orderId, e.getKey(), e.getValue()))));
            }
        }

        if (failed.isEmpty()) {
            eventPublisher.publish(EventTopic.INVENTORY_DEDUCTED_TOPIC,
                    new InventoryDeductedEvent(
                            orderId, event.customerId(), event.couponId(),
                            succeeded, event.payMethod(), event.payProvider(),
                            orderId, LocalDateTime.now()));
            log.info("[Inventory-B2] 재집계 완료(전부 성공) - orderId: {}, items: {}", orderId, succeeded.size());
        } else {
            publishDeductFailed(orderId, "재고 부족 - productIds: " + failed);
            compensate(orderId, succeeded);
            log.warn("[Inventory-B2] 재집계 완료(부분 실패) - orderId: {}, failed: {}", orderId, failed);
        }

        store.clear(orderId);
    }

    /** 성공분(HASH의 차감수량)을 정확히 그대로 REPLENISH 보상 fan-out. */
    private void compensate(String orderId, List<ItemEntry> succeeded) {
        for (ItemEntry item : succeeded) {
            String productId = item.productId().id();
            eventPublisher.publish(EventTopic.INVENTORY_STOCK_COMMAND_TOPIC,
                    new StockCommandEvent(
                            StockCommandType.REPLENISH,
                            orderId,
                            productId,
                            item.quantity().value(),
                            0,            // REPLENISH는 재집계 미발행 → totalItems 미사용
                            null, null, null, null, // 복원은 컨텍스트 불필요
                            productId,    // key = productId
                            LocalDateTime.now()));
        }
    }

    private void publishDeductFailed(String orderId, String reason) {
        eventPublisher.publish(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC,
                new InventoryDeductFailedEvent(orderId, reason, orderId, LocalDateTime.now()));
    }

    /** 집계 HASH의 성공 값(차감수량)을 파싱한다. 손상된 값이면 어느 주문/상품인지 남기고 재던진다(DLT 진단용). */
    private long parseQuantity(String orderId, String productId, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error("[Inventory-B2] 집계 HASH 값 손상 - orderId: {}, productId: {}, value: {}",
                    orderId, productId, value);
            throw e;
        }
    }
}
