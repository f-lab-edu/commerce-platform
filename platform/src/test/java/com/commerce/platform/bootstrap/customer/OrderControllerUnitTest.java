package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.order.OrderRequest;
import com.commerce.platform.bootstrap.dto.order.OrderResponse;
import com.commerce.platform.core.application.in.OrderUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
}