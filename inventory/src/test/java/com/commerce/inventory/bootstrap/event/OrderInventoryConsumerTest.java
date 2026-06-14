package com.commerce.inventory.bootstrap.event;

import com.commerce.inventory.bootstrap.dto.InventoryDeductEvent;
import com.commerce.inventory.bootstrap.dto.InventoryRestoreEvent;
import com.commerce.inventory.core.application.port.in.InventoryUseCase;
import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderInventoryConsumerTest {

    @Mock
    InventoryUseCase inventoryUseCase;
    @Mock
    TransactionalEventPublisher transactionalEventPublisher;

    OrderInventoryConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderInventoryConsumer(inventoryUseCase, transactionalEventPublisher);
    }

    private InventoryDeductEvent sampleDeduct() {
        return new InventoryDeductEvent(
                "O001", "C001", null,
                List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2))),
                "CARD", "shinHan"
        );
    }

    @DisplayName("order.created 수신 시 재고를 차감하고 InventoryDeductedEvent를 발행한다")
    @Test
    void handleOrderCreatedSuccess() {
        given(inventoryUseCase.reserve(eq("O001"), any())).willReturn(StockReserveResult.success());

        consumer.handleDeductInventory(sampleDeduct());

        verify(inventoryUseCase).reserve(eq("O001"), any());
        verify(transactionalEventPublisher)
                .publish(eq(EventTopic.INVENTORY_DEDUCTED_TOPIC), any(InventoryDeductedEvent.class));
    }

    @DisplayName("이미 처리된 주문(ALREADY_DONE)도 deducted를 발행해 saga를 이어간다")
    @Test
    void handleAlreadyDone() {
        given(inventoryUseCase.reserve(eq("O001"), any())).willReturn(StockReserveResult.alreadyDone());

        consumer.handleDeductInventory(sampleDeduct());

        verify(transactionalEventPublisher)
                .publish(eq(EventTopic.INVENTORY_DEDUCTED_TOPIC), any(InventoryDeductedEvent.class));
    }

    @DisplayName("재고 부족 시 InventoryDeductFailedEvent를 발행한다")
    @Test
    void handleInsufficient() {
        given(inventoryUseCase.reserve(eq("O001"), any()))
                .willReturn(StockReserveResult.insufficient(ProductId.of("P001")));

        consumer.handleDeductInventory(sampleDeduct());

        verify(transactionalEventPublisher)
                .publish(eq(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC), any(InventoryDeductFailedEvent.class));
        verify(transactionalEventPublisher, never())
                .publish(eq(EventTopic.INVENTORY_DEDUCTED_TOPIC), any());
    }

    @DisplayName("차감 중 BusinessException 발생 시에도 InventoryDeductFailedEvent를 발행한다")
    @Test
    void handleBusinessException() {
        given(inventoryUseCase.reserve(eq("O001"), any()))
                .willThrow(new BusinessException(BusinessError.STOCK_NOT_AVAILABLE));

        consumer.handleDeductInventory(sampleDeduct());

        verify(transactionalEventPublisher)
                .publish(eq(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC), any(InventoryDeductFailedEvent.class));
    }

    @DisplayName("보상 이벤트 수신 시 재고를 복원한다")
    @Test
    void handleCompensation() {
        given(inventoryUseCase.release(eq("O001"), any())).willReturn(true);
        InventoryRestoreEvent event = new InventoryRestoreEvent(
                "O001", new ItemEntry(ProductId.of("P001"), Quantity.create(2))
        );

        consumer.handleRestoreInventory(event);

        verify(inventoryUseCase).release(eq("O001"), any());
    }

    @DisplayName("item이 null이면 복원을 호출하지 않는다")
    @Test
    void handleCompensationEmptyItems() {
        InventoryRestoreEvent event = new InventoryRestoreEvent("O001", null);

        consumer.handleRestoreInventory(event);

        verify(inventoryUseCase, never()).release(any(), any());
    }
}
