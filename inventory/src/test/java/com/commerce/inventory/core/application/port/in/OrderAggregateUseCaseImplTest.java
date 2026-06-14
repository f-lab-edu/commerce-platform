package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.OrderAggregateStore;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
import com.commerce.shared.kafka.event.dto.OrderAggregateEvent;
import com.commerce.shared.kafka.event.dto.StockCommandEvent;
import com.commerce.shared.kafka.event.dto.StockCommandType;
import com.commerce.shared.kafka.event.topic.EventTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAggregateUseCaseImplTest {

    @Mock OrderAggregateStore store;
    @Mock TransactionalEventPublisher publisher;
    @InjectMocks OrderAggregateUseCaseImpl useCase;

    private OrderAggregateEvent ev(String orderId, String pid, long qty, boolean success, int total) {
        return new OrderAggregateEvent(orderId, pid, qty, success, total,
                "C1", "CP1", "CARD", "TOSS", orderId, LocalDateTime.now());
    }

    @Test
    @DisplayName("미완료(received < totalItems): 어떤 발행도 하지 않는다")
    void notYetComplete() {
        when(store.record("O1", any(), eq(true), eq(2L))).thenReturn(1L);
        useCase.handle(ev("O1", "P1", 2, true, 3));
        verify(publisher, never()).publish(any(), any());
        verify(store, never()).getAll(any());
    }

    @Test
    @DisplayName("전부 성공: HLEN==totalItems → inventory.deducted 발행, items는 HASH에서 재구성")
    void allSuccessFinalize() {
        when(store.record(eq("O1"), any(), eq(true), eq(3L))).thenReturn(2L);
        Map<String, String> hash = new LinkedHashMap<>();
        hash.put("P1", "2");
        hash.put("P2", "3");
        when(store.getAll("O1")).thenReturn(hash);

        useCase.handle(ev("O1", "P2", 3, true, 2));

        ArgumentCaptor<InventoryDeductedEvent> cap = ArgumentCaptor.forClass(InventoryDeductedEvent.class);
        verify(publisher).publish(eq(EventTopic.INVENTORY_DEDUCTED_TOPIC), cap.capture());
        InventoryDeductedEvent out = cap.getValue();
        assertThat(out.orderId()).isEqualTo("O1");
        assertThat(out.customerId()).isEqualTo("C1");
        assertThat(out.items()).hasSize(2);
        verify(store).clear("O1");
    }

    @Test
    @DisplayName("부분 실패: deduct-failed 발행 + 성공분만 REPLENISH fan-out")
    void partialFailureCompensates() {
        when(store.record(eq("O1"), any(), eq(false), any(Long.class))).thenReturn(2L);
        Map<String, String> hash = new LinkedHashMap<>();
        hash.put("P1", "2");   // 성공
        hash.put("P2", "F");   // 실패
        when(store.getAll("O1")).thenReturn(hash);

        useCase.handle(ev("O1", "P2", 3, false, 2));

        verify(publisher).publish(eq(EventTopic.INVENTORY_DEDUCT_FAILED_TOPIC), any());
        ArgumentCaptor<StockCommandEvent> cap = ArgumentCaptor.forClass(StockCommandEvent.class);
        verify(publisher, times(1)).publish(eq(EventTopic.INVENTORY_STOCK_COMMAND_TOPIC), cap.capture());
        StockCommandEvent comp = cap.getValue();
        assertThat(comp.type()).isEqualTo(StockCommandType.REPLENISH);
        assertThat(comp.productId()).isEqualTo("P1");
        assertThat(comp.quantity()).isEqualTo(2L);
        verify(store).clear("O1");
    }
}
