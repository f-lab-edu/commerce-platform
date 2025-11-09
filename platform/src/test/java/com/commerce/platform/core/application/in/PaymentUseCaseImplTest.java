package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PaymentOutPort;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.enums.*;
import com.commerce.platform.core.domain.service.PaymentPgRouter;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.infrastructure.persistence.OrderRepository;
import com.commerce.platform.infrastructure.pg.TossStrategy;
import com.commerce.platform.shared.exception.BusinessError;
import com.commerce.platform.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class PaymentUseCaseImplTest {
    
    @Autowired
    private PaymentUseCase paymentUseCase;

    @Autowired
    private PaymentOutPort paymentOutPort;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private PaymentPgRouter mockPaymentPgRouter;

    private Order testOrder;

    private PgPayResponse success_pgResponse = new PgPayResponse(
            "PG_TID_12345",
            "0000",
            "성공",
            true
    );

    private PgPayResponse fail_pgResponse = new PgPayResponse(
            "PG_TID_12345",
            "errorCode",
            "실패",
            false
    );

    @BeforeEach
    void init() {
        // 주문완료 상태의 신규order 저장
        testOrder = Order.create(CustomerId.of("test1"), null);
        testOrder.confirm(Money.create(35000), Money.create(0));
        orderRepository.save(testOrder);
        
        orderRepository.flush();
    }

    @Test
    void doApproval_successful() {
        PayOrderCommand command = new PayOrderCommand(
                testOrder.getOrderId(),
                null,
                null,
                null,
                PayMethod.CARD,
                PayProvider.KB,
                PaymentStatus.APPROVED
        );

        // mock pg
        PgStrategy mockPgStrategy = mock(PgStrategy.class);
        
        when(mockPaymentPgRouter.routPg(PayMethod.CARD))
                .thenReturn(mockPgStrategy);
        when(mockPgStrategy.getPgProvider())
                .thenReturn(PgProvider.TOSS);
        when(mockPgStrategy.processApproval(any()))
                .thenReturn(success_pgResponse);

        paymentUseCase.doApproval(command);

        // then 승인 성공
        Payment payment = paymentOutPort.findByOrderId(testOrder.getOrderId())
                .get();
        assertThat(payment).isNotNull();
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);

        // order 상태 검증
        assertThat(orderRepository.findById(testOrder.getOrderId()).get().getStatus())
                .isEqualTo(OrderStatus.PAID);
    }

    @Test
    void doCancel_successful() {
        // 주문 결제
        doApproval_successful();

        PayOrderCommand command = new PayOrderCommand(
                testOrder.getOrderId(),
                null,
                null,
                null,
                PayMethod.CARD,
                null,
                PaymentStatus.FULL_CANCELED
        );

        PgStrategy mockPgStrategy = mock(PgStrategy.class);

        when(mockPaymentPgRouter.getPgStrategyByProvider(any()))
                .thenReturn(mockPgStrategy);
        when(mockPgStrategy.processCancel(any()))
                .thenReturn(success_pgResponse);

        paymentUseCase.doCancel(command);

        // payment 상태
        assertThat(paymentOutPort.findByOrderId(testOrder.getOrderId())).isNotNull();
        assertThat(paymentOutPort.findByOrderId(testOrder.getOrderId()).get().getPaymentStatus())
                .isEqualTo(PaymentStatus.FULL_CANCELED);

        // order 상태 검증
        Order refundedOrder = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assertThat(refundedOrder.getStatus()).isEqualTo(OrderStatus.REFUND);
    }

    @DisplayName("전체취소실패 : 부분취소내역 존재")
    @Test
    void doCancel_failed() {
        // 결제 승인
        doApproval_successful();
        
        // 10,000원 부분취소
        PayOrderCommand partCancelCommand = new PayOrderCommand(
                testOrder.getOrderId(),
                null,
                Money.create(10000),
                null,
                PayMethod.CARD,
                null,
                null
        );

        PgStrategy mockStrategy = mock(TossStrategy.class);
        PgPayResponse partCancelResponse = new PgPayResponse(
                "PART_CANCEL_TID_001",
                "0000",
                "부분취소 성공",
                true
        );

        when(mockPaymentPgRouter.routPg(PayMethod.CARD))
                .thenReturn(mockStrategy);
        when(mockStrategy.processCancel(any()))
                .thenReturn(partCancelResponse);

        // 부분취소
        paymentUseCase.doPartCancel(partCancelCommand);

        // 전체취소
        PayOrderCommand fullCancelCommand = new PayOrderCommand(
                testOrder.getOrderId(),
                null,
                null,
                null,
                null,
                null,
                PaymentStatus.FULL_CANCELED
        );

        // 전체취소 실패
        assertThatThrownBy(() ->paymentUseCase.doCancel(fullCancelCommand))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.PAYMENT_HAS_PARTIAL_CANCEL.getMessage());

        // payment 상태 검증
        Payment payment = paymentOutPort.findByOrderId(testOrder.getOrderId()).orElseThrow();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.APPROVED);

        // order 상태  검증
        Order order = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }


}