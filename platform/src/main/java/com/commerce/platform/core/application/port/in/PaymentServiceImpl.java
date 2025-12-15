package com.commerce.platform.core.application.port.in;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.core.application.port.out.OrderItemOutPort;
import com.commerce.platform.core.application.port.out.OrderOutputPort;
import com.commerce.platform.core.application.port.out.ProductOutputPort;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.infrastructure.grpc.PaymentGrpcClient;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_ID;
import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_ITEM_ID;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentGrpcClient paymentGrpcClient;
    private final OrderOutputPort orderOutputPort;
    private final OrderItemOutPort orderItemOutPort;
    private final ProductOutputPort productOutputPort;
//    private final CustomerCardOutPort customerCardOutPort;

    @Override
    public void processApproval(PaymentRequest request) {
        // 주문 결제처리
        Order orderEntity = orderOutputPort.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validForPay();

        // payments 모듈에서 처리
        PaymentApprovalResponse grpcResponse = paymentGrpcClient.approvePayment(
                request.orderId(),
                orderEntity.getResultAmt(),
                request.installment(),
                request.payMethod(),
                request.payProvider()
        );

        // 결제 성공이면
        orderEntity.changeStatusAfterPay(grpcResponse.getSuccess()); //todo
    }

    @Override
    public void processCancel(PaymentCancelRequest request) {
        // 주문 검증
        Order orderEntity = orderOutputPort.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validateForCancel();

        // payments 모듈에서 처리 todo
        PaymentCancelResponse paymentCancelResponse = paymentGrpcClient.cancelPayment(request.orderId());

        //todo 취소 성공하면
        orderEntity.refund();

    }

    @Override
    public void processPartialCancel(PaymentCancelRequest request) {
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

        // todo 취소금액 계산, payments 호출
        Money canceledAmt = productOutputPort.findById(orderItemEntity.getProductId())
                .get()
                .getPrice().multiply(request.canceledQuantity());

        PaymentCancelResponse paymentCancelResponse = paymentGrpcClient.cancelPayment(request.orderId());

    }
}
