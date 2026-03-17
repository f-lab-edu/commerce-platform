package com.commerce.platform.core.application.port.in;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.port.out.CustomerCardOutPort;
import com.commerce.platform.core.application.port.out.OrderItemOutPort;
import com.commerce.platform.core.application.port.out.OrderOutputPort;
import com.commerce.platform.core.application.port.out.ProductOutputPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.infrastructure.grpc.PaymentGrpcClient;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_ID;
import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_ITEM_ID;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentGrpcClient paymentGrpcClient;
    private final OrderOutputPort orderOutputPort;
    private final OrderItemOutPort orderItemOutPort;
    private final ProductOutputPort productOutputPort;
    private final CustomerCardOutPort customerCardOutPort;

//    @Async tomcat thread 아닌 경우 advice에서 잡히지 않는다.
    @Override
    public CompletableFuture<String> processApproval(PaymentRequest request) {
        // 주문 검증
        Order orderEntity = orderOutputPort.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validForPay();

        // 주문 아이템 조회 (이벤트 발행에 필요)
        List<OrderItem> orderItems = orderItemOutPort.findByOrderId(orderEntity.getOrderId());
        
        // productId와 수량 매핑
        List<ProductId> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
        
        Map<ProductId, Quantity> quantities = orderItems.stream()
                .collect(Collectors.toMap(
                        OrderItem::getProductId,
                        OrderItem::getQuantity
                ));

        // gRPC 호출
        /*
        return paymentGrpcClient.approvePayment(
                request.orderId(),
                orderEntity.getResultAmt(),
                request.installment(),
                request.payMethod(),
                request.payProvider()
        ).thenApply(grpcResponse -> {
            orderEntity.changeStatusAfterPay(grpcResponse.getSuccess());
            return grpcResponse.getMessage();
        });
        */

        // 이벤트 발행 후 즉시 응답
        return CompletableFuture.completedFuture("주문결제 이벤트 발행 완료");
    }

    @Override
    public CompletableFuture<String> processCancel(PaymentCancelRequest request) {
        // 주문 검증
        Order orderEntity = orderOutputPort.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validateForCancel();

        // payments 모듈에서 처리
        return paymentGrpcClient.cancelPayment(
                request.orderId(),
                orderEntity.getResultAmt(),
                request.cancelReason(),
                "fullCanceled"
        ).thenApply(grpcResponse -> {
            orderEntity.refund();
            return "성공";
        });
    }

    @Override
    public CompletableFuture<String> processPartialCancel(PaymentCancelRequest request) {
        // 주문 검증
        Order orderEntity = orderOutputPort.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validateForCancel();

        // 부분취소 가능수량 검증
        OrderItem orderItemEntity = orderItemOutPort.findById(request.orderItemId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ITEM_ID));
        // 해당 건 삭제처리
        orderItemEntity.canceledItem(request.canceledQuantity());
        // 새롭게 행 생성한다.
        OrderItem refreshOrderItem = OrderItem.create(request.orderId(),
                orderItemEntity.getProductId(),
                orderItemEntity.getQuantity().minus(request.canceledQuantity()));
        orderItemOutPort.saveAll(List.of(refreshOrderItem));

        // 취소금액 계산
        Money canceledAmt = productOutputPort.findById(orderItemEntity.getProductId())
                .get()
                .getPrice().multiply(request.canceledQuantity());

        // payments 모듈에서 처리
        return paymentGrpcClient.cancelPayment(
                request.orderId(),
                canceledAmt,
                request.cancelReason(),
                "partialCanceled"
        ).thenApply(grpcResponse -> {
            return "성공";
        });

    }
}
