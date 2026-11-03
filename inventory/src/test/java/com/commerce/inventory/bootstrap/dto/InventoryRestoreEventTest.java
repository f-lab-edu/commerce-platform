package com.commerce.inventory.bootstrap.dto;

import com.commerce.shared.kafka.event.dto.CouponApplyFailedEvent;
import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderPriceFailedEvent;
import com.commerce.shared.kafka.event.dto.PaymentFailedEvent;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 보상 토픽 producer 이벤트(JSON) → InventoryRestoreEvent 투영이 items를 채우는지 검증한다.
 */
class InventoryRestoreEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final List<ItemEntry> ITEMS = List.of(
            new ItemEntry(ProductId.of("P1"), Quantity.create(2)),
            new ItemEntry(ProductId.of("P2"), Quantity.create(1)));

    @DisplayName("order.price-failed / coupon.apply-failed / payment.failed 페이로드를 items 리스트로 역직렬화한다")
    @Test
    void deserializeItemsFromCompensationEvents() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<DomainEvent> producerEvents = List.of(
                new OrderPriceFailedEvent("O1", "C1", null, ITEMS, "CARD", "shinHan", 0,
                        "가격 조회 실패", "O1", now),
                new CouponApplyFailedEvent("O1", "C1", "CP1", ITEMS, "CARD", "shinHan", 0,
                        10000L, "쿠폰 만료", "O1", now),
                new PaymentFailedEvent("O1", "C1", "CP1", ITEMS, "CARD", "shinHan", 0,
                        10000L, 1000L, "PG 거절", "O1", now)
        );

        for (DomainEvent producerEvent : producerEvents) {
            String json = objectMapper.writeValueAsString(producerEvent);

            InventoryRestoreEvent event = objectMapper.readValue(json, InventoryRestoreEvent.class);

            assertThat(event.orderId()).isEqualTo("O1");
            assertThat(event.items()).containsExactlyElementsOf(ITEMS);
        }
    }
}
