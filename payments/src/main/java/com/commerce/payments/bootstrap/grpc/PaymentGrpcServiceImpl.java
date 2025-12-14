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
import com.commerce.shared.vo.Quantity;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC 서버 어댑터
 * 
 * 헥사고날 아키텍처의 Inbound Adapter
 * - 외부 요청(gRPC)을 도메인 UseCase로 변환
 * - 도메인 로직은 전혀 수정하지 않음!
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
        
        log.info("[gRPC] 결제 승인 요청: orderId={}", request.getOrderId());
        
        try {
            // gRPC Request → Domain Command 변환
            PayOrderCommand command = PayOrderCommand.builder()
                    .orderId(OrderId.of(request.getOrderId()))
                    .approvedAmount(Money.of(request.getApprovedAmount()))
                    .installment(request.getInstallment())
                    .payMethod(PayMethod.valueOf(request.getPayMethod()))
                    .payProvider(PayProvider.valueOf(request.getPayProvider()))
                    .build();

            // 기존 UseCase 그대로 사용!
            paymentUseCase.doApproval(command);
            
            // 성공 응답
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
     * 전체 취소
     */
    @Override
    public void cancelPayment(
            PaymentCancelRequest request,
            StreamObserver<PaymentCancelResponse> responseObserver) {
        
        log.info("[gRPC] 전체 취소 요청: orderId={}", request.getOrderId());
        
        try {
            PayCancelCommand cancelCommand = PayCancelCommand.builder()
                    .orderId(OrderId.of(request.getOrderId()))
                    .paymentStatus(PaymentStatus.FULL_CANCELED)
                    .build();

            paymentUseCase.doCancel(cancelCommand);
            
            PaymentCancelResponse response = PaymentCancelResponse.newBuilder()
                    .setSuccess(true)
                    .setCode("0000")
                    .setMessage("전체 취소 성공")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (BusinessException e) {
            log.error("[gRPC] 전체 취소 실패: {}", e.getMessage());
            
            PaymentCancelResponse response = PaymentCancelResponse.newBuilder()
                    .setSuccess(false)
                    .setCode(e.getCode())
                    .setMessage(e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("[gRPC] 전체 취소 오류", e);
            
            PaymentCancelResponse response = PaymentCancelResponse.newBuilder()
                    .setSuccess(false)
                    .setCode("9999")
                    .setMessage("전체취소 처리 중 오류가 발생했습니다")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * 부분 취소
     */
    @Override
    public void partialCancelPayment(
            PaymentPartialCancelRequest request,
            StreamObserver<PaymentPartialCancelResponse> responseObserver) {
        
        log.info("[gRPC] 부분 취소 요청: orderId={}, itemId={}", 
                request.getOrderId(), request.getOrderItemId());
        
        try {
            // TODO: canceledAmount 계산 로직 필요 (주문 아이템 금액 * 취소 수량)
            PayCancelCommand cancelCommand = PayCancelCommand.builder()
                    .orderId(OrderId.of(request.getOrderId()))
                    .orderItemId(request.getOrderItemId())
                    .canceledQuantity(Quantity.create(request.getCanceledQuantity()))
                    .build();

            Long partCancelId = paymentUseCase.doPartCancel(cancelCommand);
            
            PaymentPartialCancelResponse response = PaymentPartialCancelResponse.newBuilder()
                    .setSuccess(true)
                    .setCode("0000")
                    .setMessage("부분 취소 성공")
                    .setData(PartialCancelData.newBuilder()
                            .setPartialCancelId(partCancelId)
                            .build())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (BusinessException e) {
            log.error("[gRPC] 부분 취소 실패: {}", e.getMessage());
            
            PaymentPartialCancelResponse response = PaymentPartialCancelResponse.newBuilder()
                    .setSuccess(false)
                    .setCode(e.getCode())
                    .setMessage(e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("[gRPC] 부분 취소 오류", e);
            
            PaymentPartialCancelResponse response = PaymentPartialCancelResponse.newBuilder()
                    .setSuccess(false)
                    .setCode("9999")
                    .setMessage("부분취소 처리 중 오류가 발생했습니다")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
