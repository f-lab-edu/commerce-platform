package com.commerce.order.core.application.port.in;

import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.enums.OrderStatus;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderCreatedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseImplTest {

    @Mock OrderOutputPort orderOutputPort;
    @Mock TransactionalEventPublisher transactionalEventPublisher;

    @InjectMocks OrderUseCaseImpl orderUseCaseImpl;

    private static List<ItemEntry> oneItem() {
        return List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2)));
    }

    @DisplayName("createOrder는 Order 애그리거트를 한 번에 저장하고 OrderCreatedEvent를 발행한다")
    @Test
    void createOrderPublishesEvent() {
        CreateOrderCommand command = new CreateOrderCommand(
            CustomerId.of("C001"), null,
            List.of(new CreateOrderCommand.OrderItemCommand(ProductId.of("P001"), Quantity.create(2))),
            "CARD", "shinHan"
        );

        OrderResponse response = orderUseCaseImpl.createOrder(command);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING.getValue());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderOutputPort).saveOrder(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getItems()).hasSize(1);
        assertThat(savedOrder.getItems().get(0).getProductId().id()).isEqualTo("P001");

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(transactionalEventPublisher).publish(eq(EventTopic.ORDER_CREATED_TOPIC), eventCaptor.capture());

        OrderCreatedEvent event = eventCaptor.getValue();
        assertThat(event.orderId()).isEqualTo(savedOrder.getOrderId().id());
        assertThat(event.customerId()).isEqualTo("C001");
        assertThat(event.couponId()).isNull();
        assertThat(event.payMethod()).isEqualTo("CARD");
        assertThat(event.payProvider()).isEqualTo("shinHan");
        assertThat(event.items()).hasSize(1);
        assertThat(event.items().get(0).productId()).isEqualTo(ProductId.of("P001"));
        assertThat(event.items().get(0).quantity()).isEqualTo(Quantity.create(2));
    }

    @DisplayName("orderCompleted는 PENDING 주문을 CONFIRMED로 전이한다")
    @Test
    void orderCompletedTransitionsPendingToConfirmed() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        given(orderOutputPort.findById(any())).willReturn(Optional.of(order));

        orderUseCaseImpl.orderCompleted(order.getOrderId(), Money.of(10000), Money.of(1000));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getOriginAmt().value()).isEqualTo(10000);
        assertThat(order.getResultAmt().value()).isEqualTo(9000);
    }
}
