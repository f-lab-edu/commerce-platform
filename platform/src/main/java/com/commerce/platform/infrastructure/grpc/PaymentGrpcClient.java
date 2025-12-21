package com.commerce.platform.infrastructure.grpc;

import com.commerce.shared.grpc.proto.*;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * Payment Service gRPC 클라이언트
 *
 */
@Slf4j
@Component
public class PaymentGrpcClient {
    
    @GrpcClient("payments")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub;
    
    /**
     * 결제 승인
     */
    public PaymentApprovalResponse approvePayment(
            OrderId orderId,
            Money approvedAmount,
            int installment,
            String payMethod,
            String payProvider) {
        
        log.info("[gRPC Client] 결제 승인 요청: orderId={}", orderId);
        
        try {
            PaymentApprovalRequest request = PaymentApprovalRequest.newBuilder()
                    .setOrderId(orderId.id())
                    .setApprovedAmount(approvedAmount.value())
                    .setInstallment(installment)
                    .setPayMethod(payMethod)
                    .setPayProvider(payProvider)
                    .build();
            
            PaymentApprovalResponse response = paymentServiceStub.approvePayment(request);
            
            log.info("[gRPC Client] 결제 승인 응답: success={}, code={}", 
                    response.getSuccess(), response.getCode());
            
            return response;
            
        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] 결제 승인 실패: {}", e.getStatus());
            throw new RuntimeException("결제 서비스 통신 실패", e);
        }
    }
    
    /**
     * 전체/부분 취소
     */
    public PaymentCancelResponse cancelPayment(OrderId orderId) {
        
        log.info("[gRPC Client] 전체 취소 요청: orderId={}", orderId);
        
        try {
           PaymentCancelRequest request = PaymentCancelRequest.newBuilder()
                    .setOrderId(orderId.id())
                    .build();
            
            PaymentCancelResponse response = paymentServiceStub.cancelPayment(request);
            
            log.info("[gRPC Client] 전체 취소 응답: success={}, code={}", 
                    response.getSuccess(), response.getCode());
            
            return response;
            
        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] 전체 취소 실패: {}", e.getStatus());
            throw new RuntimeException("결제 서비스 통신 실패", e);
        }
    }
    
    /**
     * 등록된 카드로 결제
     */
    public PaymentApprovalResponse approveWithCard(long cardId, String orderId) {
        
        log.info("[gRPC Client] 카드 결제 요청: cardId={}, orderId={}", cardId, orderId);
        
        try {
            CardPaymentRequest request = CardPaymentRequest.newBuilder()
                    .setCardId(cardId)
                    .setOrderId(orderId)
                    .build();
            
            PaymentApprovalResponse response = paymentServiceStub.approveWithCard(request);
            
            log.info("[gRPC Client] 카드 결제 응답: success={}, code={}", 
                    response.getSuccess(), response.getCode());
            
            return response;
            
        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] 카드 결제 실패: {}", e.getStatus());
            throw new RuntimeException("결제 서비스 통신 실패", e);
        }
    }
}
