package com.commerce.shared.kafka.event.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class EventDtoTest {
    @DisplayName("OrderCreatedEvent는 DomainEvent 인터페이스를 구현한다")
    @Test
    void orderCreatedEventImplementsDomainEvent() {
        List<ItemEntry> items = List.of(new ItemEntry("P001", 2));
        LocalDateTime now = LocalDateTime.now();
        OrderCreatedEvent event = new OrderCreatedEvent("O001", "C001", null, items, "CARD", "shinHan", "O001", now);
        assertThat(event).isInstanceOf(DomainEvent.class);
        assertThat(event.key()).isEqualTo("O001");
        assertThat(event.timestamp()).isEqualTo(now);
        assertThat(event.orderId()).isEqualTo("O001");
        assertThat(event.items()).hasSize(1);
    }

    @DisplayName("PaymentCompletedEvent는 originAmt와 discountAmt를 포함한다")
    @Test
    void paymentCompletedEventContainsAmounts() {
        LocalDateTime now = LocalDateTime.now();
        PaymentCompletedEvent event = new PaymentCompletedEvent("O001", 10000, 1000, "O001", now);
        assertThat(event.originAmt()).isEqualTo(10000);
        assertThat(event.discountAmt()).isEqualTo(1000);
    }

    @DisplayName("ItemEntry는 productId와 quantity를 포함한다")
    @Test
    void itemEntryContainsFields() {
        ItemEntry entry = new ItemEntry("P001", 3);
        assertThat(entry.productId()).isEqualTo("P001");
        assertThat(entry.quantity()).isEqualTo(3);
    }
}
