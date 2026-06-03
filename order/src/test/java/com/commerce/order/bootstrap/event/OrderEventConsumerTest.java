package com.commerce.order.bootstrap.event;

import com.commerce.order.bootstrap.event.dto.OrderCompletedEvent;
import com.commerce.order.bootstrap.event.dto.OrderFailedEvent;
import com.commerce.order.core.application.port.in.OrderUseCase;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock OrderUseCase orderUseCase;

    OrderEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderEventConsumer(orderUseCase);
    }

    @DisplayName("payment.completed 수신 시 orderCompleted를 호출한다")
    @Test
    void handleOrderCompleted() {
        OrderCompletedEvent event = new OrderCompletedEvent("O001", 10000L, 1000L);

        consumer.handleOrderCompleted(event);

        verify(orderUseCase).orderCompleted(eq(OrderId.of("O001")), eq(Money.of(10000)), eq(Money.of(1000)));
    }

    @DisplayName("saga 실패 이벤트 수신 시 cancelOrder를 호출한다")
    @Test
    void handleOrderFailed() {
        OrderFailedEvent event = new OrderFailedEvent("O001");

        consumer.handleOrderFailed(event, "payment.failed");

        verify(orderUseCase).cancelOrder(eq(OrderId.of("O001")), eq("주문실패:payment.failed"));
    }
}
