package com.commerce.shared.kafka.event.dto;

import com.commerce.shared.kafka.event.type.PgResult;
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
        OrderCreatedEvent event = new OrderCreatedEvent("O001", "C001", null, items, "CARD", "shinHan", 3, "O001", now);
        assertThat(event).isInstanceOf(DomainEvent.class);
        assertThat(event.key()).isEqualTo("O001");
        assertThat(event.timestamp()).isEqualTo(now);
        assertThat(event.orderId()).isEqualTo("O001");
        assertThat(event.items()).hasSize(1);
        assertThat(event.installment()).isEqualTo(3);
    }

    @DisplayName("PaymentCompletedEvent는 originAmt와 discountAmt를 포함한다")
    @Test
    void paymentCompletedEventContainsAmounts() {
        LocalDateTime now = LocalDateTime.now();
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                "O001", "C001", null, List.of(), "CARD", "shinHan", 0,
                10000, 1000,
                "PAY1", 9000, "TOSS", now,
                "O001", now);
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
    @DisplayName("InventoryReservedEvent는 주문 컨텍스트와 items를 운반하고 JSON 직렬화/역직렬화된다")
    void inventoryReservedEvent_roundTrip() throws Exception {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        InventoryReservedEvent e = new InventoryReservedEvent(
                "O1", "C1", "CP1",
                List.of(new ItemEntry(ProductId.of("P1"), Quantity.create(2))),
                "CARD", "TOSS", 0, "O1", LocalDateTime.now());

        InventoryReservedEvent back = om.readValue(om.writeValueAsString(e), InventoryReservedEvent.class);

        assertThat(back.orderId()).isEqualTo("O1");
        assertThat(back.items()).hasSize(1);
        assertThat(back.key()).isEqualTo("O1");
    }

    @Test
    @DisplayName("InventoryReserveRollbackEvent는 orderId와 items를 운반하고 JSON 직렬화/역직렬화된다")
    void inventoryReserveRollbackEvent_roundTrip() throws Exception {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        InventoryReserveRollbackEvent e = new InventoryReserveRollbackEvent(
                "O1", List.of(new ItemEntry(ProductId.of("P1"), Quantity.create(2))),
                "O1", LocalDateTime.now());

        InventoryReserveRollbackEvent back =
                om.readValue(om.writeValueAsString(e), InventoryReserveRollbackEvent.class);

        assertThat(back.orderId()).isEqualTo("O1");
        assertThat(back.items()).hasSize(1);
        assertThat(back.key()).isEqualTo("O1");
    }

    @Test
    @DisplayName("실패 이벤트는 보상용 누적 필드(items, couponId 등)와 실패 사유를 함께 운반한다")
    void failureEvent_roundTrip() throws Exception {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentFailedEvent e = new PaymentFailedEvent(
                "O1", "C1", "CP1",
                List.of(new ItemEntry(ProductId.of("P1"), Quantity.create(2))),
                "CARD", "TOSS", 0,
                10000, 1000,
                "PG_DECLINED",
                "O1", LocalDateTime.now());

        String json = om.writeValueAsString(e);
        PaymentFailedEvent back = om.readValue(json, PaymentFailedEvent.class);

        assertThat(back.reason()).isEqualTo("PG_DECLINED");
        assertThat(back.items()).hasSize(1);
        assertThat(back.couponId()).isEqualTo("CP1");
        assertThat(back.key()).isEqualTo("O1");
    }

    @Test
    @DisplayName("PaymentPgResultEvent는 PgResult와 nullable PG 필드를 직렬화/역직렬화한다")
    void paymentPgResultEvent_roundTrip() throws Exception {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentPgResultEvent e = new PaymentPgResultEvent(
                "O1", "C1", null, List.of(), "CARD", "TOSS", 0,
                10000, 1000,
                PgResult.AMOUNT_MISMATCH_VOIDED, "AMOUNT_MISMATCH", 9000, 8000L,
                "TOSS", null, "TID1", "VOID1",
                null, LocalDateTime.now(),
                "O1", LocalDateTime.now());

        PaymentPgResultEvent back = om.readValue(om.writeValueAsString(e), PaymentPgResultEvent.class);

        assertThat(back.result()).isEqualTo(PgResult.AMOUNT_MISMATCH_VOIDED);
        assertThat(back.pgApprovedAmt()).isEqualTo(8000L);
        assertThat(back.approvedAt()).isNull();
        assertThat(back.pgCancelTid()).isEqualTo("VOID1");
    }
}
