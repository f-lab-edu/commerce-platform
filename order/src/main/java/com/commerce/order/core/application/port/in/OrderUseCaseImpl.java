package com.commerce.order.core.application.port.in;

import com.commerce.order.core.application.port.in.dto.CompleteOrderCommand;
import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderDetailResponse;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.order.core.application.port.out.OrderEventPort;
import com.commerce.order.core.application.port.out.OrderOutputPort;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.vo.ItemSpec;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_ID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderUseCaseImpl implements OrderUseCase {
    private final OrderOutputPort orderOutputPort;
    private final OrderEventPort orderEventPort;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderCommand orderCommand) {
        List<ItemSpec> itemSpecs = orderCommand.orderItemCommands().stream()
                .map(item -> new ItemSpec(item.productId(), item.quantity()))
                .toList();

        Order order = Order.create(orderCommand.customerId(), orderCommand.couponId(), itemSpecs);
        orderOutputPort.saveOrder(order);

        orderEventPort.publishOrderCreated(
                order, orderCommand.payMethod(), orderCommand.payProvider(), orderCommand.installment());

        return OrderResponse.from(order);
    }

    @Override
    public List<OrderResponse> getOrders(CustomerId customerId) {
        List<Order> orders = orderOutputPort.findByCustomerId(customerId);

        return Optional.ofNullable(orders)
                .orElse(Collections.emptyList())
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Override
    public OrderDetailResponse getOrder(OrderId orderId) {
        Order order = orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));

        // todo: Task 4 이후 상품 정보는 이벤트 기반으로 대체 예정
        List<OrderDetailResponse.OrderItemResponse> items = List.of();

        return new OrderDetailResponse(
                orderId.id(),
                items,
                order.getOriginAmt().value(),
                order.getDiscountAmt().value(),
                order.getResultAmt().value(),
                order.getStatus().getValue(),
                order.getOrderedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm"))
        );
    }

    /**
     * 사용자 취소, 주문 보상트랜잭션
     */
    @Transactional
    @Override
    public OrderResponse cancelOrder(OrderId orderId, String reason) {
        Order order = orderOutputPort.findById(orderId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        order.cancel();
        return OrderResponse.ofCanceled(order);
    }

    /**
     * 결제 완료 → 금액 반영 후 주문 확정. order.confirmed는 커밋 후 발행된다.
     */
    @Transactional
    @Override
    public void orderCompleted(CompleteOrderCommand command) {
        Order order = orderOutputPort.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        order.applyAmounts(command.originAmt(), command.discountAmt());
        order.confirm();

        orderEventPort.publishOrderConfirmed(
                order, command.items(),
                command.payMethod(), command.payProvider(), command.installment(), command.paymentId());
    }
}
