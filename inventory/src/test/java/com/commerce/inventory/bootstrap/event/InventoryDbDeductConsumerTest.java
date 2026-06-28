package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.core.application.port.in.InventoryLedgerUseCase;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
import com.commerce.shared.kafka.event.dto.InventoryReserveRollbackEvent;
import com.commerce.shared.kafka.event.dto.InventoryReservedEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.topic.EventTopic;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryDbDeductConsumerTest {

    @Mock InventoryLedgerUseCase ledgerUseCase;
    @Mock TransactionalEventPublisher publisher;

    InventoryDbDeductConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InventoryDbDeductConsumer(ledgerUseCase, publisher);
    }

    private InventoryReservedEvent reserved() {
        return new InventoryReservedEvent(
                "O1", "C1", null,
                List.of(new ItemEntry(ProductId.of("P1"), Quantity.create(2))),
                "CARD", "TOSS", "O1", LocalDateTime.now());
    }

    @DisplayName("DB 차감 성공 시 inventory.deducted를 발행한다")
    @Test
    void deductSuccess_publishesDeducted() {
        consumer.onReserved(reserved());

        verify(ledgerUseCase).persistDeduction(eq("O1"), any());
        verify(publisher).publish(eq(EventTopic.INVENTORY_DEDUCTED_TOPIC), any(InventoryDeductedEvent.class));
    }

    @DisplayName("DB 재고 부족 시 deduct-failed + reserve-rollback을 발행하고 deducted는 발행하지 않는다")
    @Test
    void deductInsufficient_publishesFailedAndRollback() {
        willThrow(new BusinessException(BusinessError.INSUFFICIENT_STOCK))
                .given(ledgerUseCase).persistDeduction(eq("O1"), any());

        consumer.onReserved(reserved());

        verify(publisher).publish(eq(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC), any(InventoryDeductFailedEvent.class));
        verify(publisher).publish(eq(EventTopic.INVENTORY_RESERVE_ROLLBACK_TOPIC), any(InventoryReserveRollbackEvent.class));
        verify(publisher, never()).publish(eq(EventTopic.INVENTORY_DEDUCTED_TOPIC), any());
    }
}
