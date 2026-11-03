package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.bootstrap.dto.InventoryRestoreEvent;
import com.commerce.inventory.core.application.port.in.InventoryUseCase;
import com.commerce.shared.kafka.event.dto.InventoryReserveRollbackEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryRedisRestoreConsumerTest {

    @Mock InventoryUseCase inventoryUseCase;

    InventoryRedisRestoreConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InventoryRedisRestoreConsumer(inventoryUseCase);
    }

    @DisplayName("보상 이벤트 수신 시 주문 전체 items로 Redis 재고를 복원한다")
    @Test
    void compensation_restoresRedis() {
        List<ItemEntry> items = List.of(
                new ItemEntry(ProductId.of("P1"), Quantity.create(2)),
                new ItemEntry(ProductId.of("P2"), Quantity.create(1)));

        consumer.onCompensation(new InventoryRestoreEvent("O1", items));

        verify(inventoryUseCase).release(eq("O1"), eq(items));
    }

    @DisplayName("보상 이벤트의 items가 null/비어있으면 복원하지 않는다")
    @Test
    void compensation_emptyItems_skip() {
        consumer.onCompensation(new InventoryRestoreEvent("O1", null));
        consumer.onCompensation(new InventoryRestoreEvent("O1", List.of()));

        verify(inventoryUseCase, never()).release(any(), any());
    }

    @DisplayName("reserve-rollback 수신 시 주문 전체 items로 Redis 재고를 복원한다")
    @Test
    void reserveRollback_restoresAllItems() {
        InventoryReserveRollbackEvent event = new InventoryReserveRollbackEvent(
                "O1", List.of(new ItemEntry(ProductId.of("P1"), Quantity.create(2))),
                "O1", LocalDateTime.now());

        consumer.onReserveRollback(event);

        verify(inventoryUseCase).release(eq("O1"), any());
    }
}
