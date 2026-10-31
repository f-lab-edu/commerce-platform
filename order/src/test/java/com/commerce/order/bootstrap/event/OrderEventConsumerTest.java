package com.commerce.order.bootstrap.event;

import com.commerce.order.bootstrap.event.dto.OrderCompletedEvent;
import com.commerce.order.bootstrap.event.dto.OrderFailedEvent;
import com.commerce.order.core.application.port.in.OrderUseCase;
import com.commerce.order.core.application.port.in.dto.CompleteOrderCommand;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.PaymentCompletedEvent;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock OrderUseCase orderUseCase;

    OrderEventConsumer consumer;

    private static final List<ItemEntry> ITEMS = List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2)));

    @BeforeEach
    void setUp() {
        consumer = new OrderEventConsumer(orderUseCase);
    }

    @DisplayName("payment.completed 수신 시 saga 컨텍스트를 담아 orderCompleted를 호출한다")
    @Test
    void handleOrderCompleted() {
        OrderCompletedEvent event = new OrderCompletedEvent(
                "O001", 10000L, 1000L, "PAY001", ITEMS, "CARD", "shinHan", 0);

        consumer.handleOrderCompleted(event);

        verify(orderUseCase).orderCompleted(eq(new CompleteOrderCommand(
                OrderId.of("O001"), Money.of(10000), Money.of(1000), "PAY001",
                ITEMS, "CARD", "shinHan", 0)));
    }

    @DisplayName("payment.completed producer 페이로드(JSON)를 OrderCompletedEvent로 투영하면 필요한 필드가 모두 채워진다")
    @Test
    void paymentCompletedJsonProjectsToOrderCompletedEvent() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        PaymentCompletedEvent producerEvent = new PaymentCompletedEvent(
                "O001", "C001", "C100", ITEMS, "CARD", "shinHan", 3,
                10000L, 1000L,
                "PAY001", 9000L, "shinHan", LocalDateTime.now(),
                "O001", LocalDateTime.now());

        OrderCompletedEvent event = objectMapper.readValue(
                objectMapper.writeValueAsString(producerEvent), OrderCompletedEvent.class);

        assertThat(event).isEqualTo(new OrderCompletedEvent(
                "O001", 10000L, 1000L, "PAY001", ITEMS, "CARD", "shinHan", 3));
    }

    @DisplayName("saga 실패 이벤트 수신 시 cancelOrder를 호출한다")
    @Test
    void handleOrderFailed() {
        OrderFailedEvent event = new OrderFailedEvent("O001");

        consumer.handleOrderFailed(event, "payment.failed");

        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(orderUseCase).cancelOrder(eq(OrderId.of("O001")), reason.capture());
        assertThat(reason.getValue()).isEqualTo("주문실패:payment.failed");
    }
}
