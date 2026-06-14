package com.commerce.shared.kafka.event.dto;

import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class EventDtoTest {
    @DisplayName("OrderCreatedEvent는 DomainEvent 인터페이스를 구현한다")
    @Test
    void orderCreatedEventImplementsDomainEvent() {
        List<ItemEntry> items = List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2)));
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
        ItemEntry entry = new ItemEntry(ProductId.of("P001"), Quantity.create(3));
        assertThat(entry.productId()).isEqualTo(ProductId.of("P001"));
        assertThat(entry.quantity()).isEqualTo(Quantity.create(3));
    }

    @Test
    @DisplayName("OrderAggregateEvent는 차감수량과 주문 컨텍스트를 운반한다")
    void orderAggregateEvent_carriesQuantityAndContext() throws Exception {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        OrderAggregateEvent e = new OrderAggregateEvent(
                "O1", "P1", 3L, true, 2,
                "C1", "CP1", "CARD", "TOSS",
                "O1", LocalDateTime.now());

        OrderAggregateEvent back = om.readValue(om.writeValueAsString(e), OrderAggregateEvent.class);

        assertThat(back.quantity()).isEqualTo(3L);
        assertThat(back.success()).isTrue();
        assertThat(back.productId()).isEqualTo("P1");
        assertThat(back.totalItems()).isEqualTo(2);
        assertThat(back.customerId()).isEqualTo("C1");
        assertThat(back.key()).isEqualTo("O1");
    }

    @Test
    @DisplayName("StockCommandEvent는 주문 컨텍스트 스칼라를 운반하고 JSON 직렬화/역직렬화된다")
    void stockCommandEvent_carriesOrderContext() throws Exception {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        StockCommandEvent e = new StockCommandEvent(
                StockCommandType.DEDUCT, "O1", "P1", 3L, 2,
                "C1", "CP1", "CARD", "TOSS",
                "P1", LocalDateTime.now());

        String json = om.writeValueAsString(e);
        StockCommandEvent back = om.readValue(json, StockCommandEvent.class);

        assertThat(back.type()).isEqualTo(StockCommandType.DEDUCT);
        assertThat(back.orderId()).isEqualTo("O1");
        assertThat(back.totalItems()).isEqualTo(2);
        assertThat(back.customerId()).isEqualTo("C1");
        assertThat(back.couponId()).isEqualTo("CP1");
        assertThat(back.payMethod()).isEqualTo("CARD");
        assertThat(back.payProvider()).isEqualTo("TOSS");
        assertThat(back.key()).isEqualTo("P1");
    }
}
