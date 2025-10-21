package com.commerce.united.controller.web.order;

import com.commerce.united.port.in.OrderUseCase;
import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.port.in.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerUnitTest {
    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderController orderController;

    @Test
    void createOrder() {
        // given
        OrderRequest request = new OrderRequest("P_0001", 3);
        OrderResponse expected = new OrderResponse("ORDER_0001", "주문완료");

        when(orderUseCase.createOrder(any(OrderRequest.class)))
                .thenReturn(expected);

        // when
        OrderResponse actual = orderController.createOrder(request);

        // then
        assertThat(actual.orderId()).isEqualTo("ORDER_0001");
        assertThat(actual.status()).isEqualTo("주문완료");
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