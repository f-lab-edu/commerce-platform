package com.commerce.platform.infrastructure.grpc;

import com.commerce.shared.grpc.proto.*;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Payment Service gRPC 클라이언트
 *
 */
@Slf4j
@Component
public class PaymentGrpcClient {
    
    @GrpcClient("payments")
    private PaymentServiceGrpc.PaymentServiceFutureStub paymentServiceStub;

    /**
     * 결제 승인
     */
    public CompletableFuture<PaymentApprovalResponse> approvePayment(
            OrderId orderId,
            Money approvedAmount,
            int installment,
            String payMethod,
            String payProvider) {

        try {
            PaymentApprovalRequest request = PaymentApprovalRequest.newBuilder()
                    .setOrderId(orderId.id())
                    .setApprovedAmount(approvedAmount.value())
                    .setInstallment(installment)
                    .setPayMethod(payMethod)
                    .setPayProvider(payProvider)
                    .build();


            ListenableFuture<PaymentApprovalResponse> listenableFuture = paymentServiceStub.approvePayment(request);
            // listenableFuture -> future
            // todo 바로 바꿀 수 있는 방법을 찾아보고 해보자.
            CompletableFuture<PaymentApprovalResponse> future = new CompletableFuture<>();
            listenableFuture.addListener(() -> {
                try {
                    future.complete(listenableFuture.get());
                    log.info("[gRPC Client] 결제 승인 응답: success={}, code={}",
                            listenableFuture.get().getSuccess(), listenableFuture.get().getCode());
                } catch (InterruptedException | ExecutionException e) {
                    future.completeExceptionally(e.getCause());
                }
            },  Runnable::run);

            log.info("grpc 비동기");
            return future;

        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] 결제 승인 실패: {}", e.getStatus());
            throw new RuntimeException("결제 서비스 통신 실패", e);
        }
    }
    
    /**
     * 전체/부분 취소
     */
    public CompletableFuture<PaymentCancelResponse> cancelPayment(OrderId orderId,
                                               Money canceledAmount,
                                               String reason,
                                               String paymentStatus) {
        try {
           PaymentCancelRequest request = PaymentCancelRequest.newBuilder()
                   .setOrderId(orderId.id())
                   .setCanceledAmount(canceledAmount.value())
                   .setCancelReason(reason)
                   .setPaymentStatus(paymentStatus)
                   .build();

           ListenableFuture<PaymentCancelResponse> listenableFuture = paymentServiceStub.cancelPayment(request);
           // listenableFuture -> future
            CompletableFuture<PaymentCancelResponse> future = new CompletableFuture<>();
            listenableFuture.addListener(() -> {
                try {
                    future.complete(listenableFuture.get());
                    log.info("[gRPC Client] 취소 응답: success={}, code={}",
                            listenableFuture.get().getSuccess(), listenableFuture.get().getCode());

                } catch (InterruptedException | ExecutionException e) {
                    future.completeExceptionally(e.getCause());
                }
            },  Runnable::run);

            return future;

        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] 전체 취소 실패: {}", e.getStatus());
            throw new RuntimeException("결제 서비스 통신 실패", e);
        }
    }
    
    /**
     * 등록된 카드로 결제
     */
    public CompletableFuture<PaymentApprovalResponse> approveWithCard(long cardId, String orderId) {
        try {
            CardPaymentRequest request = CardPaymentRequest.newBuilder()
                    .setCardId(cardId)
                    .setOrderId(orderId)
                    .build();

            ListenableFuture<PaymentApprovalResponse> listenableFuture = paymentServiceStub.approveWithCard(request);
            // listenableFuture -> future
            CompletableFuture<PaymentApprovalResponse> future = new CompletableFuture<>();
            listenableFuture.addListener(() -> {
                try {
                    future.complete(listenableFuture.get());
                    log.info("[gRPC Client] 카드 결제 응답: success={}, code={}",
                            listenableFuture.get().getSuccess(), listenableFuture.get().getCode());

                } catch (InterruptedException | ExecutionException e) {
                    future.completeExceptionally(e.getCause());
                }
            },  Runnable::run);

            return future;
            
        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] 카드 결제 실패: {}", e.getStatus());
            throw new RuntimeException("결제 서비스 통신 실패", e);
        }
    }
}
