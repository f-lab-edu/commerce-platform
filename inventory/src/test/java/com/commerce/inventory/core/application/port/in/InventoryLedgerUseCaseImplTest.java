package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.InventoryStockPort;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryLedgerUseCaseImplTest {

    @Mock InventoryStockPort stockPort;

    InventoryLedgerUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new InventoryLedgerUseCaseImpl(stockPort);
    }

    private final ProductId p1 = ProductId.of("P1");
    private final ProductId p2 = ProductId.of("P2");

    private List<ItemEntry> twoItems() {
        return List.of(
                new ItemEntry(p1, Quantity.create(2)),
                new ItemEntry(p2, Quantity.create(3)));
    }

    @DisplayName("전 항목 차감 성공 시 정상 반환")
    @Test
    void persistDeduction_allSuccess() {
        given(stockPort.deductIfEnough(eq(p1), anyLong())).willReturn(1);
        given(stockPort.deductIfEnough(eq(p2), anyLong())).willReturn(1);

        useCase.persistDeduction("O1", twoItems());

        verify(stockPort).deductIfEnough(eq(p1), anyLong());
        verify(stockPort).deductIfEnough(eq(p2), anyLong());
    }

    @DisplayName("한 항목이라도 affected==0이면 BusinessException(INSUFFICIENT_STOCK)을 던진다")
    @Test
    void persistDeduction_insufficientThrows() {
        given(stockPort.deductIfEnough(eq(p1), anyLong())).willReturn(1);
        given(stockPort.deductIfEnough(eq(p2), anyLong())).willReturn(0);

        assertThatThrownBy(() -> useCase.persistDeduction("O1", twoItems()))
                .isInstanceOf(BusinessException.class)               // 재시도 안 함 정책 대상
                .extracting("code").isEqualTo("S001");               // BusinessError.INSUFFICIENT_STOCK
    }
}
