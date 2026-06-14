package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.core.application.port.in.InventoryStockCommandUseCase;
import com.commerce.shared.kafka.event.dto.StockCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * B2: 상품 단위 재고 커맨드 컨슈머 (inventory.stock-command, key=productId).
 *
 * 같은 productId는 파티션 키로 단일 스레드에 직렬화된다. 실제 DB 차감/복원과 트랜잭션 경계는
 * {@link InventoryStockCommandUseCase} 구현(@Transactional)에 있다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryStockCommandConsumer {

    private final InventoryStockCommandUseCase stockCommandUseCase;

    @KafkaListener(topics = "inventory.stock-command", groupId = "inventory-service")
    public void onStockCommand(StockCommandEvent event) {
        log.debug("[Inventory-B2] stock-command 수신 - type: {}, orderId: {}, productId: {}",
                event.type(), event.orderId(), event.productId());
        stockCommandUseCase.handle(event);
    }
}
