package com.commerce.inventory;

import com.commerce.inventory.core.application.port.out.InventoryStockCache;
import com.commerce.inventory.core.domain.vo.StockReserveResult;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 기반 재고 차감 동시성 통합 테스트 (B1).
 * deduct는 멱등성 미보장(추후 outbox), restore는 복원 1회만 보장한다.
 * docker-compose up -d (Redis 필요) 후 실행.
 */
@Slf4j
@SpringBootTest
class InventoryStockConcurrencyIntegrationTest {

    private static final String PRODUCT = "Pconc00000001";

    @Autowired
    private InventoryStockCache stockCache;
    @Autowired
    private StringRedisTemplate redis;

    private final ProductId productId = ProductId.of(PRODUCT);

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        redis.delete("inventory:stock:" + PRODUCT);
        var restored = redis.keys("inventory:restored:*");
        if (restored != null && !restored.isEmpty()) redis.delete(restored);
    }

    private List<ItemEntry> oneUnit() {
        return List.of(new ItemEntry(productId, Quantity.create(1)));
    }

    @DisplayName("재고 100, 동시 주문 150 → 정확히 100건만 성공하고 재고는 0, 오버셀 없음")
    @Test
    void noOversellUnderConcurrency() throws InterruptedException {
        int stock = 100;
        int orders = 150;
        stockCache.set(productId, stock);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch ready = new CountDownLatch(orders);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger insufficient = new AtomicInteger();

        for (int i = 0; i < orders; i++) {
            String orderId = "OC" + i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    StockReserveResult r = stockCache.deduct(orderId, oneUnit());
                    if (r.status() == StockReserveResult.Status.SUCCESS) success.incrementAndGet();
                    else if (r.status() == StockReserveResult.Status.INSUFFICIENT) insufficient.incrementAndGet();
                } catch (Exception e) {
                    log.error("deduct 실패 - orderId: {}", orderId, e);
                }
            });
        }
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue();

        assertThat(success.get()).as("성공 건수 = 재고 수량").isEqualTo(stock);
        assertThat(insufficient.get()).as("나머지는 재고 부족").isEqualTo(orders - stock);
        assertThat(stockCache.current(productId)).as("최종 재고 0, 음수 없음").isZero();
    }

    @DisplayName("같은 주문을 두 번 차감하면 (멱등성 미보장) 두 번 모두 차감된다 — 정확히-한-번은 추후 outbox로 보강")
    @Test
    void deductIsNotIdempotentYet() {
        stockCache.set(productId, 10);

        StockReserveResult first = stockCache.deduct("Odup", List.of(new ItemEntry(productId, Quantity.create(3))));
        StockReserveResult second = stockCache.deduct("Odup", List.of(new ItemEntry(productId, Quantity.create(3))));

        assertThat(first.status()).isEqualTo(StockReserveResult.Status.SUCCESS);
        assertThat(second.status()).as("마커 제거로 재전달 시 재차감").isEqualTo(StockReserveResult.Status.SUCCESS);
        assertThat(stockCache.current(productId)).as("두 번 차감 = 6 감소(보수적 under-count)").isEqualTo(4);
    }

    @DisplayName("복원은 1회만 적용된다(중복 복원 방지)")
    @Test
    void restoreOnce() {
        stockCache.set(productId, 5);

        boolean first = stockCache.restore("Ores", oneUnit());
        boolean second = stockCache.restore("Ores", oneUnit());

        assertThat(first).as("최초 복원 성공").isTrue();
        assertThat(second).as("중복 복원은 무시").isFalse();
        assertThat(stockCache.current(productId)).as("한 번만 +1").isEqualTo(6);
    }

    @DisplayName("멀티상품 중 하나라도 부족하면 전부 차감하지 않는다(all-or-nothing)")
    @Test
    void multiItemAllOrNothing() {
        ProductId p2 = ProductId.of("Pconc00000002");
        try {
            stockCache.set(productId, 5);
            stockCache.set(p2, 1);

            // p1 3개(가능), p2 2개(부족) → 전부 실패해야 함
            StockReserveResult r = stockCache.deduct("Omulti", List.of(
                    new ItemEntry(productId, Quantity.create(3)),
                    new ItemEntry(p2, Quantity.create(2))
            ));

            assertThat(r.status()).isEqualTo(StockReserveResult.Status.INSUFFICIENT);
            assertThat(r.failedProductId()).isEqualTo(p2);
            assertThat(stockCache.current(productId)).as("p1 미차감").isEqualTo(5);
            assertThat(stockCache.current(p2)).as("p2 미차감").isEqualTo(1);
        } finally {
            redis.delete("inventory:stock:Pconc00000002");
        }
    }
}
