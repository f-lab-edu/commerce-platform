package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.bootstrap.dto.InventoryRestoreEvent;
import com.commerce.inventory.core.application.port.in.InventoryLedgerUseCase;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryDbRestoreConsumerTest {

    @Mock InventoryLedgerUseCase ledgerUseCase;

    InventoryDbRestoreConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InventoryDbRestoreConsumer(ledgerUseCase);
    }

    @DisplayName("보상 이벤트 수신 시 주문 전체 items로 DB 원장을 복원한다")
    @Test
    void compensation_restoresDb() {
        List<ItemEntry> items = List.of(
                new ItemEntry(ProductId.of("P1"), Quantity.create(2)),
                new ItemEntry(ProductId.of("P2"), Quantity.create(1)));

        consumer.onCompensation(new InventoryRestoreEvent("O1", items));

        verify(ledgerUseCase).persistRestoration(eq("O1"), eq(items));
    }

    @DisplayName("items가 null/비어있으면 복원하지 않는다")
    @Test
    void compensation_emptyItems_skip() {
        consumer.onCompensation(new InventoryRestoreEvent("O1", null));
        consumer.onCompensation(new InventoryRestoreEvent("O1", List.of()));

        verify(ledgerUseCase, never()).persistRestoration(any(), any());
    }
}
