package com.commerce.united.port.service;

import com.commerce.united.port.in.dto.OrderRefundRequest;
import com.commerce.united.port.in.dto.OrderRefundResponse;
import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.port.in.dto.OrderResponse;
import com.commerce.united.port.out.OrderOutputPort;
import com.commerce.united.domain.aggregate.Order;
import com.commerce.united.domain.aggregate.OrderItem;
import com.commerce.united.domain.vo.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {
    @Mock
    private OrderOutputPort orderOutputPort;

    @InjectMocks
    private OrderService orderService;

    private List<OrderItem> orderItems;
    private Order order;

    @BeforeEach
    void init() throws Exception {
        String prefix = "P_000";
        List<OrderItem> orderItems = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            orderItems.add(OrderItem.create(prefix + i, 1004, i + 1));
        }

        Order order = Order.create("userId", orderItems);

        this.order = order;
        this.orderItems = orderItems;
    }

    @Test
    void createOrder() throws Exception {
        OrderRequest request = new OrderRequest("P_0001", 3);

        // given : mock 동작 정의
        when(orderOutputPort.saveOrder(any(OrderRequest.class)))
                .thenReturn(order);

        // when
        OrderResponse response = orderService.createOrder(request);

        // then
        assertThat(response.orderId()).isEqualTo(order.getOrderId());
        assertThat(response.status()).isEqualTo(order.getStatus().getValue());
    }

    @Test
    void getOrder() throws Exception {
        // given : mock 동작 정의
        when(orderOutputPort.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(order));

        // when
        OrderResponse response = orderService.getOrder(this.order.getOrderId());

        // then
        assertThat(response.orderId()).isEqualTo(order.getOrderId());
    }

    @Test
    void cancelOrder() {
        String reason = "단순변심";

        // given : mock 동작 정의
        order.changeStatus(OrderStatus.CANCELED);

        when(orderOutputPort.updateOrder(any(String.class), eq(reason), eq(OrderStatus.CANCELED)))
                .thenReturn(order);

        // when
        OrderResponse response = orderService.cancelOrder(order.getOrderId(), reason);

        // then
        assertThat(response.status()).isEqualTo(order.getStatus().getValue());
    }

    @Test
    void refundOrder() {
        String reason = "환불사유";

        // given : mock 동작 정의
        order.changeStatus(OrderStatus.REFUND);
        when(orderOutputPort.updateOrder(any(String.class), eq(reason), eq(OrderStatus.REFUND)))
                .thenReturn(order);

        // when
        OrderRefundRequest request = new OrderRefundRequest(reason, "KB",
                "1111", "신정은");
        OrderRefundResponse response = orderService.refundOrder(order.getOrderId(), request);

        assertThat(response.status()).isEqualTo(order.getStatus().getValue());
    }
}