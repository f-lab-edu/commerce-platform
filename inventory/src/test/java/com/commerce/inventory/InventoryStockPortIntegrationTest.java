package com.commerce.inventory;

import com.commerce.inventory.core.application.port.out.InventoryStockPort;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.infrastructure.persistence.InventoryRepository;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryStockPortIntegrationTest {

    @Autowired private InventoryStockPort stockPort;
    @Autowired private InventoryRepository repository;

    private final ProductId pid = ProductId.of("Pb2portTEST1");

    @BeforeEach
    void setUp() {
        repository.save(Inventory.builder()
                .productId(pid).quantity(Quantity.create(10)).updatedAt(LocalDateTime.now()).build());
    }

    @AfterEach
    void tearDown() {
        repository.deleteById(pid);
    }

    @Test
    @DisplayName("재고 충분: deductIfEnough → 1 반환, 수량 감소")
    void deduct_success() {
        int affected = stockPort.deductIfEnough(pid, 4);
        assertThat(affected).isEqualTo(1);
        assertThat(repository.findByProductId(pid).orElseThrow().getQuantity().value()).isEqualTo(6);
    }

    @Test
    @DisplayName("재고 부족: deductIfEnough → 0 반환, 수량 불변(오버셀 없음)")
    void deduct_insufficient() {
        int affected = stockPort.deductIfEnough(pid, 11);
        assertThat(affected).isZero();
        assertThat(repository.findByProductId(pid).orElseThrow().getQuantity().value()).isEqualTo(10);
    }

    @Test
    @DisplayName("복원: replenish → 수량 증가")
    void replenish_increases() {
        stockPort.deductIfEnough(pid, 4); // 10 → 6
        int affected = stockPort.replenish(pid, 4); // 6 → 10
        assertThat(affected).isEqualTo(1);
        assertThat(repository.findByProductId(pid).orElseThrow().getQuantity().value()).isEqualTo(10);
    }
}
