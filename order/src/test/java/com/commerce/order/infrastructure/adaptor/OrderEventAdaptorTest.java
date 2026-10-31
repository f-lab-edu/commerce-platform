package com.commerce.order.infrastructure.adaptor;

import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.vo.ItemSpec;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderConfirmedEvent;
import com.commerce.shared.kafka.event.dto.OrderCreatedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.CouponId;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventAdaptorTest {

    @Mock TransactionalEventPublisher transactionalEventPublisher;

    @InjectMocks OrderEventAdaptor orderEventAdaptor;

    private static List<ItemSpec> oneItem() {
        return List.of(new ItemSpec(ProductId.of("P001"), Quantity.create(3)));
    }

    private static List<ItemEntry> oneEntry() {
        return List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(3)));
    }

    @DisplayName("publishOrderCreated는 Order의 items와 외부 컨텍스트로 OrderCreatedEvent를 조립해 발행한다")
    @Test
    void publishOrderCreatedAssemblesEvent() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());

        orderEventAdaptor.publishOrderCreated(order, "CARD", "shinHan", 3);

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(transactionalEventPublisher).publish(eq(EventTopic.ORDER_CREATED_TOPIC), captor.capture());

        OrderCreatedEvent event = captor.getValue();
        assertThat(event.orderId()).isEqualTo(order.getOrderId().id());
        assertThat(event.customerId()).isEqualTo("C001");
        assertThat(event.couponId()).isNull();
        assertThat(event.payMethod()).isEqualTo("CARD");
        assertThat(event.payProvider()).isEqualTo("shinHan");
        assertThat(event.installment()).isEqualTo(3);
        assertThat(event.key()).isEqualTo(order.getOrderId().id());
        assertThat(event.items()).containsExactlyElementsOf(oneEntry());
    }

    @DisplayName("publishOrderConfirmed는 확정된 Order 금액과 saga 컨텍스트로 OrderConfirmedEvent를 조립해 발행한다")
    @Test
    void publishOrderConfirmedAssemblesEvent() {
        Order order = Order.create(CustomerId.of("C001"), CouponId.of("C100"), oneItem());
        order.applyAmounts(Money.of(10000), Money.of(1000));
        order.confirm();

        orderEventAdaptor.publishOrderConfirmed(order, oneItem(), "CARD", "shinHan", 0, "PAY001");

        ArgumentCaptor<OrderConfirmedEvent> captor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
        verify(transactionalEventPublisher).publish(eq(EventTopic.ORDER_CONFIRMED_TOPIC), captor.capture());

        OrderConfirmedEvent event = captor.getValue();
        assertThat(event.orderId()).isEqualTo(order.getOrderId().id());
        assertThat(event.customerId()).isEqualTo("C001");
        assertThat(event.couponId()).isEqualTo("C100");
        assertThat(event.items()).containsExactlyElementsOf(oneEntry());
        assertThat(event.payMethod()).isEqualTo("CARD");
        assertThat(event.originAmt()).isEqualTo(10000L);
        assertThat(event.discountAmt()).isEqualTo(1000L);
        assertThat(event.resultAmt()).isEqualTo(9000L);
        assertThat(event.paymentId()).isEqualTo("PAY001");
        assertThat(event.key()).isEqualTo(order.getOrderId().id());
    }
}
