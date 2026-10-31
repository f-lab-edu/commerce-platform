package com.commerce.order.core.application.port.in;

import com.commerce.order.core.application.port.in.dto.CompleteOrderCommand;
import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.order.core.application.port.out.OrderEventPort;
import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.enums.OrderStatus;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseImplTest {

    @Mock OrderOutputPort orderOutputPort;
    @Mock OrderEventPort orderEventPort;

    @InjectMocks OrderUseCaseImpl orderUseCaseImpl;

    private static List<ItemEntry> oneItem() {
        return List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2)));
    }

    private static CompleteOrderCommand completeCommand(OrderId orderId) {
        return new CompleteOrderCommand(
                orderId, Money.of(10000), Money.of(1000), "PAY001",
                oneItem(), "CARD", "shinHan", 0
        );
    }

    @DisplayName("createOrder는 Order 애그리거트를 한 번에 저장하고 주문 생성 이벤트 발행을 포트에 위임한다")
    @Test
    void createOrderPublishesEvent() {
        CreateOrderCommand command = new CreateOrderCommand(
            CustomerId.of("C001"), null,
            List.of(new CreateOrderCommand.OrderItemCommand(ProductId.of("P001"), Quantity.create(2))),
            "CARD", "shinHan", 0
        );

        OrderResponse response = orderUseCaseImpl.createOrder(command);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING.getValue());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderOutputPort).saveOrder(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getItems()).hasSize(1);
        assertThat(savedOrder.getItems().get(0).getProductId().id()).isEqualTo("P001");

        verify(orderEventPort).publishOrderCreated(eq(savedOrder), eq("CARD"), eq("shinHan"), eq(0));
    }

    @DisplayName("orderCompleted는 PENDING 주문을 CONFIRMED로 전이하고 주문 확정 이벤트 발행을 포트에 위임한다")
    @Test
    void orderCompletedTransitionsPendingToConfirmed() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        given(orderOutputPort.findById(any())).willReturn(Optional.of(order));

        orderUseCaseImpl.orderCompleted(completeCommand(order.getOrderId()));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getOriginAmt().value()).isEqualTo(10000);
        assertThat(order.getResultAmt().value()).isEqualTo(9000);
        verify(orderEventPort).publishOrderConfirmed(
                eq(order), eq(oneItem()), eq("CARD"), eq("shinHan"), eq(0), eq("PAY001"));
    }

    @DisplayName("orderCompleted는 이미 취소된 주문이면 예외를 던지고 주문 확정 이벤트를 발행하지 않는다")
    @Test
    void orderCompletedOnCanceledOrderDoesNotPublish() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        order.cancel();
        given(orderOutputPort.findById(any())).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderUseCaseImpl.orderCompleted(completeCommand(order.getOrderId())))
                .isInstanceOf(BusinessException.class);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        verify(orderEventPort, never()).publishOrderConfirmed(
                any(), any(), anyString(), anyString(), anyInt(), anyString());
    }
}
