package com.commerce.inventory;

import com.commerce.inventory.core.application.port.in.OrderInventoryFanoutUseCase;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.infrastructure.persistence.InventoryRepository;
import com.commerce.shared.kafka.event.dto.InventoryDeductFailedEvent;
import com.commerce.shared.kafka.event.dto.InventoryDeductedEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B2 scatter-gather end-to-end 통합 테스트 (실제 Kafka + Redis + MySQL).
 *
 * fanout 서비스를 직접 호출(order.created 미경유)해 stock-command → (DB 차감) → order-aggregate
 * → (Redis HASH 재집계) → inventory.deducted 전체 사슬을 검증한다.
 * docker-compose up -d 후 실행.
 */
@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryB2ScatterGatherIntegrationTest {

    @Autowired private OrderInventoryFanoutUseCase fanout;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private StringRedisTemplate redis;
    @Autowired private Collector collector;

    @TestConfiguration
    static class TestListenerConfig {
        @Bean
        Collector b2TestCollector() {
            return new Collector();
        }
    }

    static class Collector {
        final Set<String> deducted = ConcurrentHashMap.newKeySet();
        final Set<String> failed = ConcurrentHashMap.newKeySet();

        @KafkaListener(topics = "inventory.deducted", groupId = "inventory-b2-test-collector")
        void onDeducted(InventoryDeductedEvent e) { deducted.add(e.orderId()); }

        @KafkaListener(topics = "inventory.deduct-failed", groupId = "inventory-b2-test-collector")
        void onFailed(InventoryDeductFailedEvent e) { failed.add(e.orderId()); }
    }

    private final ProductId pA = ProductId.of("Pb2e2eA0001");
    private final ProductId pB = ProductId.of("Pb2e2eB0001");
    private final ProductId pC = ProductId.of("Pb2e2eC0001");
    private final ProductId pHot = ProductId.of("Pb2e2eHOT01");

    @BeforeAll
    void warmup() throws InterruptedException {
        ProductId pw = ProductId.of("Pb2e2eWARM01");
        String warmOrder = "Ob2e2eWARM" + System.nanoTime();
        seedDb(pw, 100);
        fanout.fanout(warmOrder, "C0", null,
                List.of(new ItemEntry(pw, Quantity.create(1))), "CARD", "TOSS");
        waitUntil(() -> collector.deducted.contains(warmOrder), 60_000);
        inventoryRepository.deleteById(pw);
        clearAggKeys();
    }

    @BeforeEach
    void setUp() { clearAggKeys(); }

    @AfterEach
    void tearDown() { clearAggKeys(); }

    private void seedDb(ProductId pid, long qty) {
        inventoryRepository.save(Inventory.builder()
                .productId(pid).quantity(Quantity.create(qty)).updatedAt(LocalDateTime.now()).build());
    }

    private long dbStock(ProductId pid) {
        return inventoryRepository.findByProductId(pid).map(i -> i.getQuantity().value()).orElse(-1L);
    }

    private void clearAggKeys() {
        var keys = redis.keys("inventory:agg:Ob2e2e*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }

    private void waitUntil(BooleanSupplier cond, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (cond.getAsBoolean()) return;
            Thread.sleep(100);
        }
        // 조건 미충족으로 타임아웃하면 부분 상태에서 후속 단언이 false-green 될 수 있으므로 큰 소리로 실패시킨다.
        throw new AssertionError("waitUntil 타임아웃(" + timeoutMs + "ms) - 조건 미충족");
    }

    @DisplayName("정상: 3개 상품 전부 충분 → inventory.deducted 발행, 각 상품 DB 수량만큼 차감")
    @Test
    void happyPath() throws InterruptedException {
        seedDb(pA, 10); seedDb(pB, 10); seedDb(pC, 10);
        String orderId = "Ob2e2eOK" + System.nanoTime();

        fanout.fanout(orderId, "C1", null, List.of(
                new ItemEntry(pA, Quantity.create(2)),
                new ItemEntry(pB, Quantity.create(3)),
                new ItemEntry(pC, Quantity.create(4))
        ), "CARD", "TOSS");

        waitUntil(() -> collector.deducted.contains(orderId), 30_000);

        assertThat(collector.deducted).as("inventory.deducted 수신").contains(orderId);
        assertThat(collector.failed).doesNotContain(orderId);
        assertThat(dbStock(pA)).isEqualTo(8);
        assertThat(dbStock(pB)).isEqualTo(7);
        assertThat(dbStock(pC)).isEqualTo(6);

        inventoryRepository.deleteById(pA);
        inventoryRepository.deleteById(pB);
        inventoryRepository.deleteById(pC);
    }

    @DisplayName("부분 실패: C 재고 부족 → inventory.deduct-failed 발행 + 성공분(A,B) DB 복원")
    @Test
    void partialFailureCompensates() throws InterruptedException {
        seedDb(pA, 10); seedDb(pB, 10); seedDb(pC, 1); // C 부족(2 요청)
        String orderId = "Ob2e2eFAIL" + System.nanoTime();

        fanout.fanout(orderId, "C1", null, List.of(
                new ItemEntry(pA, Quantity.create(2)),
                new ItemEntry(pB, Quantity.create(3)),
                new ItemEntry(pC, Quantity.create(2))
        ), "CARD", "TOSS");

        waitUntil(() -> collector.failed.contains(orderId), 30_000);
        assertThat(collector.failed).as("inventory.deduct-failed 수신").contains(orderId);
        assertThat(collector.deducted).doesNotContain(orderId);

        // 보상 REPLENISH는 추가 stock-command 라운드트립 → 재고 원복까지 대기
        waitUntil(() -> dbStock(pA) == 10 && dbStock(pB) == 10, 30_000);
        assertThat(dbStock(pA)).as("A 복원").isEqualTo(10);
        assertThat(dbStock(pB)).as("B 복원").isEqualTo(10);
        assertThat(dbStock(pC)).as("C는 차감된 적 없어 그대로").isEqualTo(1);

        inventoryRepository.deleteById(pA);
        inventoryRepository.deleteById(pB);
        inventoryRepository.deleteById(pC);
    }

    @DisplayName("오버셀 없음(Kafka 경유): 핫 상품 재고 50, 동시 주문 80 → 50건만 deducted, DB 0, 음수 없음")
    @Test
    void noOversellViaKafka() throws InterruptedException {
        int stock = 50;
        int orders = 80;
        seedDb(pHot, stock);

        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch ready = new CountDownLatch(orders);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger published = new AtomicInteger();

        for (int i = 0; i < orders; i++) {
            String orderId = "Ob2e2eHOT" + i + "_" + System.nanoTime();
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    fanout.fanout(orderId, "C1", null,
                            List.of(new ItemEntry(pHot, Quantity.create(1))), "CARD", "TOSS");
                    published.incrementAndGet();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        assertThat(published.get()).as("모든 fanout 호출이 인터럽트 없이 완료").isEqualTo(orders);

        // 모든 주문이 deducted 또는 failed로 종결될 때까지 대기
        waitUntil(() -> deductedHot() + failedHot() >= orders, 60_000);

        assertThat(dbStock(pHot)).as("재고 정확히 소진(음수 없음)").isZero();
        assertThat(deductedHot()).as("정확히 재고만큼만 성공").isEqualTo(stock);

        inventoryRepository.deleteById(pHot);
    }

    private long deductedHot() {
        return collector.deducted.stream().filter(o -> o.startsWith("Ob2e2eHOT")).count();
    }

    private long failedHot() {
        return collector.failed.stream().filter(o -> o.startsWith("Ob2e2eHOT")).count();
    }
}
