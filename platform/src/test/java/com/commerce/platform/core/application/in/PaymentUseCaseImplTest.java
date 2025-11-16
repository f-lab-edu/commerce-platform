package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PaymentOutPort;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.enums.*;
import com.commerce.platform.core.domain.service.PaymentPgRouter;
import com.commerce.platform.core.domain.vo.*;
import com.commerce.platform.infrastructure.persistence.OrderItemRepository;
import com.commerce.platform.infrastructure.persistence.OrderRepository;
import com.commerce.platform.infrastructure.persistence.PaymentPartCancelRepository;
import com.commerce.platform.shared.exception.BusinessError;
import com.commerce.platform.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentPartCancelRepository paymentPartCancelRepository;

    @MockBean
    private PaymentPgRouter mockPaymentPgRouter;

    private Order testOrder;

    private List<OrderItem> testOrderItems;

    private final ProductId productId1 = ProductId.of("P20251110222600000001");
    private final ProductId productId2 = ProductId.of("P20251110222600000002");

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

        testOrderItems = List.of(
            OrderItem.create(testOrder.getOrderId(), productId1, Quantity.create(3)),
            OrderItem.create(testOrder.getOrderId(), productId2, Quantity.create(1))
        );

        orderItemRepository.saveAll(testOrderItems);
        orderItemRepository.flush();
    }

    @DisplayName("주문 결제 성공")
    @Test
    void doApproval_successful() {
        PayOrderCommand command = new PayOrderCommand(
                testOrder.getOrderId(),
                null,
                null,
                PayMethod.CARD,
                PayProvider.KB
        );

        mockPgStrategy();

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

    @DisplayName("전체환불 성공")
    @Test
    void doCancel_successful() {
        // 주문 결제
        doApproval_successful();

        // 전체취소 요청
        PayCancelCommand command = PayCancelCommand.builder()
                .orderId(testOrder.getOrderId())
                .paymentStatus(PaymentStatus.FULL_CANCELED)
                .build();

        mockPgStrategy();

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
        
        mockPgStrategy();

        // 부분취소 요청
        OrderItem canceledItem = testOrderItems.get(0);

        PayCancelCommand partCancelCommand = PayCancelCommand.builder()
                .orderId(testOrder.getOrderId())
                .orderItemId(canceledItem.getId())
                .canceledQuantity(Quantity.create(2))
                .paymentStatus(PaymentStatus.PARTIAL_CANCELED)
                .build();

        mockPgStrategy();

        // 부분취소
        Long partCancelId = paymentUseCase.doPartCancel(partCancelCommand);

        // 전체취소
        PayCancelCommand fullCancelCommand = PayCancelCommand.builder()
                .orderId(testOrder.getOrderId())
                .paymentStatus(PaymentStatus.FULL_CANCELED)
                .build();

        // 전체취소 실패
        assertThatThrownBy(() -> paymentUseCase.doCancel(fullCancelCommand))
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


    @DisplayName("부분취소 성공")
    @Test
    void doPartCancel_successful() {
        // 주문 결제
        doApproval_successful();

        // 부분취소 요청
        OrderItem canceledItem = testOrderItems.get(0);

        PayCancelCommand partCancelCommand = PayCancelCommand.builder()
                .orderId(testOrder.getOrderId())
                .orderItemId(canceledItem.getId())
                .canceledQuantity(Quantity.create(2))
                .paymentStatus(PaymentStatus.PARTIAL_CANCELED)
                .build();

        mockPgStrategy();

        // 부분취소
        Long partCancelId = paymentUseCase.doPartCancel(partCancelCommand);

        // payment 상태
        assertThat(paymentOutPort.findByOrderId(testOrder.getOrderId()).get().getPaymentStatus())
                .as("원거래는 승인상태 유지")
                .isEqualTo(PaymentStatus.APPROVED);

        assertThat(paymentPartCancelRepository.findById(partCancelId))
                .isPresent()
                .get()
                .satisfies(partCancel -> {
                    assertThat(partCancel)
                            .as("insert 부분취소")
                            .isNotNull();

                    assertThat(partCancel.getCanceledAmt().value())
                            .as("부분취소 금액 검증")
                            .isEqualTo(2500 * 2) ;
                });

        // orderitem 검증
        assertThat(orderItemRepository.findById(canceledItem.getId()))
                .isPresent()
                .get()
                .satisfies(
                        orderItem -> {
                            assertThat(orderItem.isCanceled()).isEqualTo(true);
                        }
                );

        assertThat(orderItemRepository.findByOrderIdAndProductIdAndCanceled(partCancelCommand.getOrderId(), canceledItem.getProductId(), false))
                .isPresent()
                .get()
                .satisfies(oi -> {
                    assertThat(oi.getQuantity().value())
                            .as("새로 추가된 orderitem 수량 확인")
                            .isEqualTo(canceledItem.getQuantity().minus(partCancelCommand.getCanceledQuantity()).value());
                    assertThat(oi.isCanceled()).isEqualTo(false);
                });

        // order 상태 검증
        Order refundedOrder = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assertThat(refundedOrder.getStatus())
                .as("주문상태는 결제완료로 유지")
                .isEqualTo(OrderStatus.PAID);
    }

    @DisplayName("부분취소 실패 : 취소 가능 수량 초과")
    @Test
    void doPartCancel_failed() {
        // 주문 결제
        doApproval_successful();

        // 부분취소 요청
        PayCancelCommand partCancelCommand = PayCancelCommand.builder()
                .orderId(testOrder.getOrderId())
                .orderItemId(testOrderItems.get(1).getId())
                .canceledQuantity(Quantity.create(2))
                .paymentStatus(PaymentStatus.PARTIAL_CANCELED)
                .build();

        mockPgStrategy();

        // 부분취소
        assertThatThrownBy(() -> paymentUseCase.doPartCancel(partCancelCommand))
                .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(BusinessError.INVALID_CANCELED_QUANTITY.getMessage());

        assertThat(paymentOutPort.findByOrderId(testOrder.getOrderId()).get().getPaymentStatus())
                .as("원거래는 승인상태 유지")
                .isEqualTo(PaymentStatus.APPROVED);

        PaymentId paymentId = paymentOutPort.findByOrderId(testOrder.getOrderId()).get().getPaymentId();
        assertThat(paymentPartCancelRepository.existsPaymentPartCancelByApprovedPaymentId(paymentId))
                .as("not insert 부분취소")
                .isEqualTo(false);

        // order 상태 검증
        Order refundedOrder = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assertThat(refundedOrder.getStatus()).isEqualTo(OrderStatus.PAID)
                .as("주문상태는 결제완료로 유지");
    }

    private void mockPgStrategy() {
        PgStrategy mockPgStrategy = mock(PgStrategy.class);

        when(mockPaymentPgRouter.getPgStrategyByProvider(any()))
                .thenReturn(mockPgStrategy);

        when(mockPgStrategy.processCancel(any()))
                .thenReturn(success_pgResponse);

        when(mockPaymentPgRouter.routePg(PayMethod.CARD, PayProvider.KB))
                .thenReturn(mockPgStrategy);

        when(mockPgStrategy.getPgProvider())
                .thenReturn(PgProvider.TOSS);

        when(mockPgStrategy.processApproval(any()))
                .thenReturn(success_pgResponse);
    }
}