package com.commerce.inventory.core.application.port.in;

import com.commerce.inventory.core.application.port.out.InventoryStockPort;
import com.commerce.inventory.core.application.port.out.ProcessedEventPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryLedgerUseCaseImplTest {

    @Mock InventoryStockPort stockPort;
    @Mock ProcessedEventPort processedEventPort;

    InventoryLedgerUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new InventoryLedgerUseCaseImpl(stockPort, processedEventPort);
    }

    private final ProductId p1 = ProductId.of("P1");
    private final ProductId p2 = ProductId.of("P2");

    private List<ItemEntry> twoItems() {
        return List.of(
                new ItemEntry(p1, Quantity.create(2)),
                new ItemEntry(p2, Quantity.create(3)));
    }

    @DisplayName("전 항목 차감 성공 시 markProcessed 후 정상 반환")
    @Test
    void persistDeduction_allSuccess() {
        given(processedEventPort.exists("O1:LEDGER-DEDUCT")).willReturn(false);
        given(stockPort.deductIfEnough(eq(p1), anyLong())).willReturn(1);
        given(stockPort.deductIfEnough(eq(p2), anyLong())).willReturn(1);

        useCase.persistDeduction("O1", twoItems());

        verify(processedEventPort).markProcessed("O1:LEDGER-DEDUCT");
    }

    @DisplayName("한 항목이라도 affected==0이면 BusinessException(INSUFFICIENT_STOCK)을 던지고 markProcessed하지 않는다")
    @Test
    void persistDeduction_insufficientThrows() {
        given(processedEventPort.exists("O1:LEDGER-DEDUCT")).willReturn(false);
        given(stockPort.deductIfEnough(eq(p1), anyLong())).willReturn(1);
        given(stockPort.deductIfEnough(eq(p2), anyLong())).willReturn(0);

        assertThatThrownBy(() -> useCase.persistDeduction("O1", twoItems()))
                .isInstanceOf(BusinessException.class)               // 재시도 안 함 정책 대상
                .extracting("code").isEqualTo("S001");               // BusinessError.INSUFFICIENT_STOCK

        verify(processedEventPort, never()).markProcessed("O1:LEDGER-DEDUCT");
    }

    @DisplayName("이미 처리된 주문은 skip(멱등)")
    @Test
    void persistDeduction_idempotentSkip() {
        given(processedEventPort.exists("O1:LEDGER-DEDUCT")).willReturn(true);

        useCase.persistDeduction("O1", twoItems());

        verify(stockPort, never()).deductIfEnough(eq(p1), anyLong());
        verify(processedEventPort, never()).markProcessed("O1:LEDGER-DEDUCT");
    }
}
