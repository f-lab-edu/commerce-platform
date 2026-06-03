package com.commerce.order.core.domain.aggregate;

import com.commerce.order.core.domain.enums.OrderStatus;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.OrderCreatedEvent;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static List<Order.ItemSpec> oneItem() {
        return List.of(new Order.ItemSpec(ProductId.of("P001"), Quantity.create(2)));
    }

    @DisplayName("주문 생성 시 PENDING 상태이다")
    @Test
    void createOrderIsPending() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOriginAmt().value()).isZero();
    }

    @DisplayName("create()는 ItemSpec으로 OrderItem까지 함께 생성한다")
    @Test
    void createBuildsItemsFromSpec() {
        Order order = Order.create(
                CustomerId.of("C001"),
                null,
                List.of(
                        new Order.ItemSpec(ProductId.of("P001"), Quantity.create(2)),
                        new Order.ItemSpec(ProductId.of("P002"), Quantity.create(1))
                )
        );

        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems().get(0).getOrderId()).isEqualTo(order.getOrderId());
        assertThat(order.getItems().get(0).getProductId().id()).isEqualTo("P001");
        assertThat(order.getItems().get(0).getQuantity().value()).isEqualTo(2);
        assertThat(order.getItems().get(1).getProductId().id()).isEqualTo("P002");
    }

    @DisplayName("create()는 items가 비어있으면 예외를 던진다")
    @Test
    void createFailsWhenItemsEmpty() {
        assertThatThrownBy(() -> Order.create(CustomerId.of("C001"), null, List.of()))
                .isInstanceOf(BusinessException.class);
    }

    @DisplayName("create()는 items가 null이면 예외를 던진다")
    @Test
    void createFailsWhenItemsNull() {
        assertThatThrownBy(() -> Order.create(CustomerId.of("C001"), null, null))
                .isInstanceOf(BusinessException.class);
    }

    @DisplayName("getItems()는 불변 뷰를 반환한다")
    @Test
    void getItemsReturnsImmutableView() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        assertThatThrownBy(() -> order.getItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @DisplayName("toCreatedEvent()는 Order의 items와 외부 컨텍스트로 이벤트를 조립한다")
    @Test
    void toCreatedEventAssemblesEvent() {
        Order order = Order.create(
                CustomerId.of("C001"),
                null,
                List.of(new Order.ItemSpec(ProductId.of("P001"), Quantity.create(3)))
        );

        OrderCreatedEvent event = order.toCreatedEvent("CARD", "shinHan");

        assertThat(event.orderId()).isEqualTo(order.getOrderId().id());
        assertThat(event.customerId()).isEqualTo("C001");
        assertThat(event.couponId()).isNull();
        assertThat(event.payMethod()).isEqualTo("CARD");
        assertThat(event.payProvider()).isEqualTo("shinHan");
        assertThat(event.key()).isEqualTo(order.getOrderId().id());
        assertThat(event.items()).hasSize(1);
        assertThat(event.items().get(0).productId()).isEqualTo("P001");
        assertThat(event.items().get(0).quantity()).isEqualTo(3);
    }

    @DisplayName("applyAmounts로 금액을 세팅하면 resultAmt가 계산된다")
    @Test
    void applyAmountsCalculatesResult() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        order.applyAmounts(Money.of(10000), Money.of(1000));
        assertThat(order.getOriginAmt().value()).isEqualTo(10000);
        assertThat(order.getDiscountAmt().value()).isEqualTo(1000);
        assertThat(order.getResultAmt().value()).isEqualTo(9000);
    }

    @DisplayName("confirm()은 금액이 세팅된 PENDING 주문을 CONFIRMED로 전이한다")
    @Test
    void confirmTransitionsPendingToConfirmed() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        order.applyAmounts(Money.of(10000), Money.of(0));
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @DisplayName("confirm()은 금액이 0이면 예외를 던진다")
    @Test
    void confirmFailsWhenAmountIsZero() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        assertThatThrownBy(order::confirm).isInstanceOf(BusinessException.class);
    }

    @DisplayName("cancel()은 PENDING 상태에서 예외를 던진다")
    @Test
    void cancelFromPendingFails() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        assertThatThrownBy(order::cancel).isInstanceOf(BusinessException.class);
    }

    @DisplayName("cancel()은 CONFIRMED 상태에서 CANCELED로 전이한다")
    @Test
    void cancelFromConfirmed() {
        Order order = Order.create(CustomerId.of("C001"), null, oneItem());
        order.applyAmounts(Money.of(10000), Money.of(0));
        order.confirm();
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }
}
