package com.commerce.united.port.service;

import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.port.in.dto.OrderResponse;
import com.commerce.united.port.out.OrderOutputPort;
import com.commerce.united.domain.aggregate.Order;
import com.commerce.united.domain.aggregate.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceTest {
    @MockitoBean
    private OrderOutputPort orderOutputPort;

    @Autowired
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
    void createOrder() {
        OrderRequest request = new OrderRequest("P_0001", 3);

        // given
        when(orderOutputPort.saveOrder(request))
                .thenReturn(order);

        // when
        OrderResponse response = orderService.createOrder(request);

        // then
        assertThat(response.orderId()).isEqualTo(order.getOrderId());
        assertThat(response.status()).isEqualTo(order.getStatus().getValue());
    }

    @Test
    void getOrder() {
    }

    @Test
    void cancelOrder() {
    }

    @Test
    void refundOrder() {
    }
}