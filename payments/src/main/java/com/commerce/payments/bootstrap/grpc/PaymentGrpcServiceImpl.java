package com.commerce.payments.bootstrap.grpc;

import com.commerce.payments.application.port.in.PaymentUseCase;
import com.commerce.payments.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.payments.domain.enums.PayProvider;
import com.commerce.payments.domain.enums.PaymentStatus;
import com.commerce.payments.grpc.proto.*;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC 서버 어댑터
 */
@Slf4j
@RequiredArgsConstructor
@GrpcService
public class PaymentGrpcServiceImpl extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final PaymentUseCase paymentUseCase;

    /**
     * 결제 승인 - 기존 REST API를 gRPC로 변경
     */
    @Override
    public void approvePayment(
            PaymentApprovalRequest request,
            StreamObserver<PaymentApprovalResponse> responseObserver) {

        try {
            // gRPC Request → Domain Command
            PayOrderCommand command = PayOrderCommand.builder()
                    .orderId(OrderId.of(request.getOrderId()))
                    .approvedAmount(Money.of(request.getApprovedAmount()))
                    .installment(request.getInstallment())
                    .payMethod(PayMethod.valueOf(request.getPayMethod()))
                    .payProvider(PayProvider.valueOf(request.getPayProvider()))
                    .build();

            paymentUseCase.doApproval(command);
            
            // 응답
            PaymentApprovalResponse response = PaymentApprovalResponse.newBuilder()
                    .setSuccess(true)
                    .setCode("0000")
                    .setMessage("결제 승인 성공")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (BusinessException e) {
            log.error("[gRPC] 결제 승인 실패: {}", e.getMessage());
            
            PaymentApprovalResponse response = PaymentApprovalResponse.newBuilder()
                    .setSuccess(false)
                    .setCode(e.getCode())
                    .setMessage(e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("[gRPC] 결제 승인 오류", e);
            
            PaymentApprovalResponse response = PaymentApprovalResponse.newBuilder()
                    .setSuccess(false)
                    .setCode("9999")
                    .setMessage("승인 처리 중 오류가 발생했습니다")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * 전체/부분 취소
     */
    @Override
    public void cancelPayment(
            PaymentCancelRequest request,
            StreamObserver<PaymentCancelResponse> responseObserver) {

        try {
            PayCancelCommand cancelCommand = PayCancelCommand.builder()
                    .orderId(OrderId.of(request.getOrderId()))
                    .paymentStatus(PaymentStatus.valueOf(request.getPaymentStatus()))
                    .canceledAmount(Money.of(request.getCanceledAmount()))
                    .cancelReason(request.getCancelReason())
                    .build();

            paymentUseCase.doCancel(cancelCommand);
            
            PaymentCancelResponse response = PaymentCancelResponse.newBuilder()
                    .setSuccess(true)
                    .setCode("0000")
                    .setMessage("취소 성공")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (BusinessException e) {
            log.error("[gRPC] 취소 실패: {}", e.getMessage());
            
            PaymentCancelResponse response = PaymentCancelResponse.newBuilder()
                    .setSuccess(false)
                    .setCode(e.getCode())
                    .setMessage(e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("[gRPC] 취소 오류", e);
            
            PaymentCancelResponse response = PaymentCancelResponse.newBuilder()
                    .setSuccess(false)
                    .setCode("9999")
                    .setMessage("취소 처리 중 오류가 발생했습니다")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
