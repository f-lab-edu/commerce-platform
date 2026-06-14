package com.commerce.inventory.core.application.port.in;

import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.StockCommandEvent;
import com.commerce.shared.kafka.event.dto.StockCommandType;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * B2 fan-out: 주문을 상품 단위 stock-command(DEDUCT)로 분해만 한다. 차감은 하지 않는다.
 *
 * 주문 레벨 컨텍스트(customerId/couponId/payMethod/payProvider)를 각 이벤트에 실어 보내
 * finalize까지 운반한다(별도 저장 없음). fan-out 멱등 등 컨슈머 멱등은 추후 일괄 적용(스펙 §12).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderInventoryFanoutUseCaseImpl implements OrderInventoryFanoutUseCase {

    private final TransactionalEventPublisher eventPublisher;

    @Override
    public void fanout(String orderId, String customerId, String couponId,
                       List<ItemEntry> items, String payMethod, String payProvider) {
        if (items == null || items.isEmpty()) {
            log.warn("[Inventory-B2] fan-out skip(빈 items) - orderId: {}", orderId);
            return;
        }

        int totalItems = items.size();
        for (ItemEntry item : items) {
            String productId = item.productId().id();
            eventPublisher.publish(EventTopic.INVENTORY_STOCK_COMMAND_TOPIC,
                    new StockCommandEvent(
                            StockCommandType.DEDUCT,
                            orderId,
                            productId,
                            item.quantity().value(),
                            totalItems,
                            customerId,
                            couponId,
                            payMethod,
                            payProvider,
                            productId, // key = productId (같은 상품 직렬화)
                            LocalDateTime.now()
                    ));
        }
        log.info("[Inventory-B2] fan-out 완료 - orderId: {}, items: {}", orderId, totalItems);
    }
}
