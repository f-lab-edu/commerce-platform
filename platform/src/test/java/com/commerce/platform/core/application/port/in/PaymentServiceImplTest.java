package com.commerce.platform.core.application.port.in;

import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.port.out.CustomerCardOutPort;
import com.commerce.platform.core.application.port.out.OrderItemOutPort;
import com.commerce.platform.core.application.port.out.OrderOutputPort;
import com.commerce.platform.core.application.port.out.ProductOutputPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PaymentServiceImplTest {
    @Autowired
    private PaymentService paymentService;

    @MockBean
    private OrderOutputPort orderOutputPort;

    @MockBean
    private OrderItemOutPort orderItemOutPort;

    @MockBean
    private ProductOutputPort productOutputPort;

    @MockBean
    private CustomerCardOutPort customerCardOutPort;

    @Test
    @DisplayName("gRPC payments서버와 통신 성공")
    void processApproval() throws ExecutionException, InterruptedException {
        // given
        OrderId orderId = OrderId.of("O20251228150200000000");
        PaymentRequest request = new PaymentRequest(
                orderId,
                "CARD",
                "samsung",
                0
        );

        Order mockOrder = createMockOrder(orderId, Money.of(10000));
        given(orderOutputPort.findById(orderId)).willReturn(Optional.of(mockOrder));

        // when
        String result = paymentService.processApproval(request).get();

        // then
        verify(orderOutputPort).findById(orderId);
        verify(mockOrder).changeStatusAfterPay(any(Boolean.class));
    }

    private Order createMockOrder(OrderId orderId, Money amount) {
        Order mockOrder = org.mockito.Mockito.mock(Order.class);
        given(mockOrder.getResultAmt()).willReturn(amount);
        return mockOrder;
    }

}