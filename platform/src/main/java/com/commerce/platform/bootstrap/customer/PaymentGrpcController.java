package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.payment.PaymentCancelRequest;
import com.commerce.platform.bootstrap.dto.payment.PaymentRequest;
import com.commerce.platform.grpc.proto.PaymentApprovalResponse;
import com.commerce.platform.grpc.proto.PaymentCancelResponse;
import com.commerce.platform.grpc.proto.PaymentPartialCancelResponse;
import com.commerce.platform.infrastructure.grpc.PaymentGrpcClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * gRPC 기반 결제 컨트롤러
 * 
 * REST API는 그대로 유지하되, 내부 구현은 gRPC 통신으로 변경
 * - 클라이언트는 변경 없음
 * - 내부 아키텍처만 MSA로 전환
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/payments")
@RestController
public class PaymentGrpcController {
    
    private final PaymentGrpcClient paymentGrpcClient;
    
    /**
     * 결제 승인
     */
    @PostMapping("/approval")
    public ResponseEntity<Map<String, Object>> approvePayment(
            @Valid @RequestBody PaymentRequest request) {
        
        log.info("[API Gateway] 결제 승인 요청: orderId={}", request.orderId());
        
        try {
            // gRPC 클라이언트로 payments 서비스 호출
            PaymentApprovalResponse grpcResponse = paymentGrpcClient.approvePayment(
                    request.orderId(),
                    request.installment(),
                    request.payMethod(),
                    request.payProvider()
            );
            
            // gRPC 응답을 REST 응답으로 변환
            Map<String, Object> response = new HashMap<>();
            response.put("success", grpcResponse.getSuccess());
            response.put("code", grpcResponse.getCode());
            response.put("message", grpcResponse.getMessage());
            
            if (grpcResponse.hasData()) {
                Map<String, Object> data = new HashMap<>();
                data.put("paymentId", grpcResponse.getData().getPaymentId());
                data.put("approvedAmount", grpcResponse.getData().getApprovedAmount());
                data.put("approvedAt", grpcResponse.getData().getApprovedAt());
                data.put("payMethod", grpcResponse.getData().getPayMethod());
                data.put("status", grpcResponse.getData().getStatus());
                response.put("data", data);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[API Gateway] 결제 승인 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "9999");
            errorResponse.put("message", "결제 처리 중 오류가 발생했습니다");
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * 전체 취소
     */
    @PatchMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(
            @Valid @RequestBody PaymentCancelRequest request) {
        
        log.info("[API Gateway] 전체 취소 요청: orderId={}", request.orderId());
        
        try {
            PaymentCancelResponse grpcResponse = paymentGrpcClient.cancelPayment(
                    request.orderId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", grpcResponse.getSuccess());
            response.put("code", grpcResponse.getCode());
            response.put("message", grpcResponse.getMessage());
            
            if (grpcResponse.hasData()) {
                Map<String, Object> data = new HashMap<>();
                data.put("paymentId", grpcResponse.getData().getPaymentId());
                data.put("canceledAmount", grpcResponse.getData().getCanceledAmount());
                data.put("canceledAt", grpcResponse.getData().getCanceledAt());
                response.put("data", data);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[API Gateway] 전체 취소 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "9999");
            errorResponse.put("message", "전체취소 처리 중 오류가 발생했습니다");
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * 부분 취소
     */
    @PatchMapping("/partial-cancel")
    public ResponseEntity<Map<String, Object>> partialCancelPayment(
            @Valid @RequestBody PaymentCancelRequest request) {
        
        log.info("[API Gateway] 부분 취소 요청: orderId={}, itemId={}", 
                request.orderId(), request.orderItemId());
        
        try {
            PaymentPartialCancelResponse grpcResponse = paymentGrpcClient.partialCancelPayment(
                    request.orderId(),
                    request.orderItemId(),
                    request.canceledQuantity()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", grpcResponse.getSuccess());
            response.put("code", grpcResponse.getCode());
            response.put("message", grpcResponse.getMessage());
            
            if (grpcResponse.hasData()) {
                Map<String, Object> data = new HashMap<>();
                data.put("partialCancelId", grpcResponse.getData().getPartialCancelId());
                data.put("canceledAmount", grpcResponse.getData().getCanceledAmount());
                data.put("remainAmount", grpcResponse.getData().getRemainAmount());
                data.put("canceledAt", grpcResponse.getData().getCanceledAt());
                response.put("data", data);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[API Gateway] 부분 취소 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "9999");
            errorResponse.put("message", "부분취소 처리 중 오류가 발생했습니다");
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * 등록된 카드로 결제
     */
    @PostMapping("/registry-card/{cardId}")
    public ResponseEntity<Map<String, Object>> approveWithCard(
            @PathVariable Long cardId,
            @RequestBody Map<String, String> body) {
        
        String orderId = body.get("orderId");
        log.info("[API Gateway] 카드 결제 요청: cardId={}, orderId={}", cardId, orderId);
        
        try {
            PaymentApprovalResponse grpcResponse = paymentGrpcClient.approveWithCard(
                    cardId,
                    orderId
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", grpcResponse.getSuccess());
            response.put("code", grpcResponse.getCode());
            response.put("message", grpcResponse.getMessage());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[API Gateway] 카드 결제 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "9999");
            errorResponse.put("message", "카드 결제 처리 중 오류가 발생했습니다");
            return ResponseEntity.ok(errorResponse);
        }
    }
}
