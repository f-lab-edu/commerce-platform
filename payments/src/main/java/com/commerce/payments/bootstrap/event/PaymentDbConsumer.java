package com.commerce.payments.bootstrap.event;

import com.commerce.payments.core.application.port.in.PaymentUseCase;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.domain.aggregate.Payment;
import com.commerce.payments.core.domain.enums.PgApprovalStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.PaymentCompletedEvent;
import com.commerce.shared.kafka.event.dto.PaymentFailedEvent;
import com.commerce.shared.kafka.event.dto.PaymentPgResultEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 결제 DB 단계 컨슈머. PG를 호출하지 않는다.
 *
 * payment.pg-result 수신 →
 * - APPROVED: payment 저장 커밋 後 payment.completed 발행
 * - 그 외: payment_failure 저장 커밋 後 payment.failed 발행 (order/inventory/coupon 보상 트리거)
 * 저장 유스케이스가 @Transactional이라 정상 반환 시점엔 이미 커밋 완료.
 * DB 예외는 throw → ErrorHandler 재시도 → DLT. PG 재호출이 없어 재시도해도 이중결제 없음.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentDbConsumer {

    private final PaymentUseCase paymentUseCase;
    private final TransactionalEventPublisher publisher;

    @KafkaListener(topics = "payment.pg-result", groupId = "payments-db")
    public void onPgResult(PaymentPgResultEvent event) {
        log.info("[Payments-DB] payment.pg-result 수신 - orderId: {}, result: {}", event.orderId(), event.result());

        PgApprovalResult result = toApprovalResult(event);

        if (result.isApproved()) {
            PayOrderCommand command = PayOrderCommand.of(
                    event.orderId(), event.requestedAmt(), event.payMethod(), event.payProvider(), event.installment());
            Payment payment = paymentUseCase.saveApprovedPayment(command, result);

            publisher.publish(EventTopic.PAYMENT_COMPLETED_TOPIC,
                    new PaymentCompletedEvent(
                            event.orderId(), event.customerId(), event.couponId(),
                            event.items(), event.payMethod(), event.payProvider(), event.installment(),
                            event.originAmt(), event.discountAmt(),
                            payment.getPaymentId().id(), payment.getApprovedAmt().value(),
                            payment.getPgProvider().name(), payment.getApprovedAt(),
                            event.orderId(), LocalDateTime.now()));
            log.info("[Payments-DB] 결제 저장·completed 발행 - orderId: {}", event.orderId());
            return;
        }

        paymentUseCase.savePaymentFailure(OrderId.of(event.orderId()), result);

        publisher.publish(EventTopic.PAYMENT_FAILED_TOPIC,
                new PaymentFailedEvent(
                        event.orderId(), event.customerId(), event.couponId(),
                        event.items(), event.payMethod(), event.payProvider(), event.installment(),
                        event.originAmt(), event.discountAmt(),
                        event.reason(),
                        event.orderId(), LocalDateTime.now()));
        log.info("[Payments-DB] 실패 저장·failed 발행 - orderId: {}, result: {}", event.orderId(), event.result());
    }

    private PgApprovalResult toApprovalResult(PaymentPgResultEvent event) {
        return PgApprovalResult.builder()
                .status(PgApprovalStatus.valueOf(event.result().name()))
                .pgProvider(event.pgProvider() == null ? null : PgProvider.getByPgName(event.pgProvider()))
                .pgTid(event.pgTid())
                .pgCancelTid(event.pgCancelTid())
                .pgApprovedAmt(event.pgApprovedAmt())
                .responseCode(event.pgResponseCode())
                .reason(event.reason())
                .approvedAt(event.approvedAt())
                .canceledAt(event.canceledAt())
                .build();
    }
}
