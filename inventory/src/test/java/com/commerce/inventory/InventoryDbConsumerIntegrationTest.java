package com.commerce.inventory;

import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.infrastructure.persistence.InventoryRepository;
import com.commerce.inventory.core.infrastructure.persistence.ProcessedEventRepository;
import com.commerce.shared.kafka.KafkaEventPublisher;
import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryReservedEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 신규 4-컨슈머 배선 end-to-end (실제 Kafka + MySQL). docker-compose up -d 후 실행.
 * - inventory.reserved → B → DB 차감 + inventory.deducted
 * - DB 재고 부족 → 차감 거절(원장 무변경)
 * - order.price-failed → D → DB 원장 복원
 */
@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryDbConsumerIntegrationTest {

    /** Collects orderId values from inventory.deduct-failed for deterministic failure detection. */
    @TestConfiguration
    static class DeductFailedCollectorConfig {
        @Bean
        DeductFailedCollector deductFailedCollector() {
            return new DeductFailedCollector();
        }
    }

    static class DeductFailedCollector {
        final Set<String> receivedOrderIds =
                Collections.newSetFromMap(new ConcurrentHashMap<>());

        @KafkaListener(topics = "inventory.deduct-failed", groupId = "inv-deductfailed-test-collector")
        public void onDeductFailed(InventoryDeductFailedEvent event) {
            receivedOrderIds.add(event.orderId());
        }
    }

    @Autowired private KafkaEventPublisher publisher;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;
    @Autowired private DeductFailedCollector deductFailedCollector;

    @BeforeAll
    void warmup() throws InterruptedException {
        ProductId pw = ProductId.of("PrsvWARM01");
        String warmOrder = "OrsvWARM" + System.nanoTime();
        seedDb(pw, 100);

        long deadline = System.currentTimeMillis() + 60_000;
        boolean consumed = false;
        while (System.currentTimeMillis() < deadline) {
            publishReserved(warmOrder, pw, 1);
            if (waitFor(() -> dbStock(pw) == 99, 3_000)) { consumed = true; break; }
        }
        if (!consumed) throw new AssertionError("warmup: inventory-db-deduct 그룹이 60s 내 소비를 시작하지 못함");
        cleanup(warmOrder, pw);
    }

    @DisplayName("inventory.reserved 수신 → B가 DB 원장에서 차감한다")
    @Test
    void reservedDeductsDb() throws InterruptedException {
        ProductId p = ProductId.of("PrsvDED01");
        String orderId = "OrsvDED" + System.nanoTime();
        seedDb(p, 10);

        publishReserved(orderId, p, 3);

        waitUntil(() -> dbStock(p) == 7, 30_000);
        assertThat(dbStock(p)).as("원장 차감").isEqualTo(7);
        assertThat(processedEventRepository.existsById(orderId + ":LEDGER-DEDUCT")).isTrue();

        cleanup(orderId, p);
    }

    @DisplayName("DB 재고 부족 → B가 차감을 거절하고 원장은 변하지 않는다(all-or-nothing)")
    @Test
    void insufficientRejectsDeduct() throws InterruptedException {
        ProductId p = ProductId.of("PrsvINS01");
        String orderId = "OrsvINS" + System.nanoTime();
        seedDb(p, 2); // 요청 3 > 재고 2

        publishReserved(orderId, p, 3);
        // Wait for the deduct-failed signal (B publishes it on INSUFFICIENT_STOCK) — deterministic.
        waitUntil(() -> deductFailedCollector.receivedOrderIds.contains(orderId), 30_000);

        assertThat(dbStock(p)).as("원장 무변경").isEqualTo(2);
        assertThat(processedEventRepository.existsById(orderId + ":LEDGER-DEDUCT")).isFalse();

        cleanup(orderId, p);
    }

    @DisplayName("order.price-failed 수신(차감 선행) → D가 DB 원장을 복원한다")
    @Test
    void compensationRestoresDb() throws InterruptedException {
        ProductId p = ProductId.of("PrsvRES01");
        String orderId = "OrsvRES" + System.nanoTime();
        seedDb(p, 10);

        publishReserved(orderId, p, 3);
        waitUntil(() -> dbStock(p) == 7, 30_000);

        publisher.publish(EventTopic.ORDER_PRICE_FAILED_TOPIC,
                new RestoreSignal(orderId, new ItemEntry(p, Quantity.create(3)), orderId, LocalDateTime.now()));

        waitUntil(() -> dbStock(p) == 10, 30_000);
        assertThat(dbStock(p)).as("원장 복원").isEqualTo(10);
        assertThat(processedEventRepository.existsById(orderId + ":LEDGER-RESTORE")).isTrue();

        cleanup(orderId, p);
    }

    // --- helpers ---

    private void publishReserved(String orderId, ProductId p, long qty) {
        publisher.publish(EventTopic.INVENTORY_RESERVED_TOPIC,
                new InventoryReservedEvent(orderId, "C1", null,
                        List.of(new ItemEntry(p, Quantity.create(qty))),
                        "CARD", "TOSS", orderId, LocalDateTime.now()));
    }

    private void seedDb(ProductId pid, long qty) {
        inventoryRepository.save(Inventory.builder()
                .productId(pid).quantity(Quantity.create(qty)).updatedAt(LocalDateTime.now()).build());
    }

    private long dbStock(ProductId pid) {
        return inventoryRepository.findByProductId(pid).map(i -> i.getQuantity().value()).orElse(-1L);
    }

    private void cleanup(String orderId, ProductId pid) {
        deleteProcessed(orderId + ":LEDGER-DEDUCT");
        deleteProcessed(orderId + ":LEDGER-RESTORE");
        if (inventoryRepository.existsById(pid)) inventoryRepository.deleteById(pid);
    }

    private void deleteProcessed(String eventId) {
        if (processedEventRepository.existsById(eventId)) processedEventRepository.deleteById(eventId);
    }

    private void waitUntil(BooleanSupplier cond, long timeoutMs) throws InterruptedException {
        if (!waitFor(cond, timeoutMs)) throw new AssertionError("waitUntil 타임아웃(" + timeoutMs + "ms)");
    }

    private boolean waitFor(BooleanSupplier cond, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (cond.getAsBoolean()) return true;
            Thread.sleep(100);
        }
        return false;
    }

    /** Test-local publish carrier — keeps InventoryRestoreEvent a plain consume-only projection. */
    private record RestoreSignal(String orderId, ItemEntry item, String key, java.time.LocalDateTime timestamp)
            implements DomainEvent { }
}
