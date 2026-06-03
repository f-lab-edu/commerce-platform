package com.commerce.order.core.application.port.in;

import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.enums.OrderStatus;
import com.commerce.order.infrastructure.adaptor.OrderAdaptor;
import com.commerce.order.infrastructure.persistence.OrderRepository;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({OrderUseCaseImpl.class, OrderAdaptor.class})
class OrderUseCaseImplIntegrationTest {

    @MockitoBean TransactionalEventPublisher transactionalEventPublisher;

    @Autowired OrderUseCase orderUseCase;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderOutputPort orderOutputPort;

    private OrderId savePendingOrder() {
        Order order = Order.create(
                CustomerId.of("test-customer-id2"),
                null,
                List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2)))
        );
        orderOutputPort.saveOrder(order);
        return order.getOrderId();
    }

    @DisplayName("orderCompleted 성공: PENDING 주문이 CONFIRMED + 금액과 함께 DB에 반영된다")
    @Test
    void orderCompletedSuccess() {
        OrderId orderId = savePendingOrder();

        orderUseCase.orderCompleted(orderId, Money.of(10000), Money.of(1000));

        Order persisted = orderRepository.findById(orderId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(persisted.getOriginAmt().value()).isEqualTo(10000L);
        assertThat(persisted.getDiscountAmt().value()).isEqualTo(1000L);
        assertThat(persisted.getResultAmt().value()).isEqualTo(9000L);
        assertThat(persisted.getUpdatedAt()).isNotNull();
    }

    @DisplayName("orderCompleted 실패: 이미 CONFIRMED 주문에 재호출 시 INVALID_ORDER_STATUS, DB 상태 유지")
    @Test
    void orderCompletedFailureOnAlreadyConfirmed() {
        OrderId orderId = savePendingOrder();
        orderUseCase.orderCompleted(orderId, Money.of(10000), Money.of(1000));

        assertThatThrownBy(() -> orderUseCase.orderCompleted(orderId, Money.of(20000), Money.of(2000)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", BusinessError.INVALID_ORDER_STATUS.getCode());

        Order persisted = orderRepository.findById(orderId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(persisted.getOriginAmt().value()).isEqualTo(10000L);
        assertThat(persisted.getDiscountAmt().value()).isEqualTo(1000L);
        assertThat(persisted.getResultAmt().value()).isEqualTo(9000L);
    }

    @DisplayName("cancelOrder 성공: 주문이 CANCELED 상태로 DB에 반영된다")
    @Test
    void cancelOrderSuccess() {
        OrderId orderId = savePendingOrder();

        orderUseCase.cancelOrder(orderId, "사용자취소");

        Order persisted = orderRepository.findById(orderId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(persisted.getUpdatedAt()).isNotNull();
    }

    @DisplayName("cancelOrder 실패: 존재하지 않는 orderId 시 INVALID_ORDER_ID, DB 변경 없음")
    @Test
    void cancelOrderFailureOnNotFound() {
        OrderId unknownId = OrderId.of("O99999999999999999999");

        assertThatThrownBy(() -> orderUseCase.cancelOrder(unknownId, "취소"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", BusinessError.INVALID_ORDER_ID.getCode());

        assertThat(orderRepository.findById(unknownId)).isEmpty();
    }
}
