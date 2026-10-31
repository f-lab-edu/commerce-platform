package com.commerce.payments.bootstrap.event;

import com.commerce.payments.core.application.port.in.PaymentUseCase;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.CouponAppliedEvent;
import com.commerce.shared.kafka.event.dto.PaymentPgResultEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.kafka.event.type.PgResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 결제 PG 단계 컨슈머. PG만 호출하고 DB는 다루지 않는다.
 *
 * coupon.applied 수신 → PG 승인(금액 불일치/결과 불명 시 망취소까지) → payment.pg-result 발행.
 * DB 저장은 payment.pg-result 를 구독하는 {@link PaymentDbConsumer}가 한다.
 * (DB 저장 실패로 재시도될 때 PG 재승인 = 이중결제를 막기 위해 단계를 분리)
 *
 * 에러 처리 정책:
 * - PG 호출 전 예외(요청값 변환 실패, PG 라우팅 실패): throw → ErrorHandler 재시도 → DLT. PG 미호출이라 재시도해도 안전.
 * - PG 호출 이후: approveWithPg가 예외 없이 결과로 반환하므로 coupon.applied 재시도로 이어지지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentPgConsumer {

    private final PaymentUseCase paymentUseCase;
    private final TransactionalEventPublisher publisher;

    @KafkaListener(topics = "coupon.applied", groupId = "payments-pg")
    public void onCouponApplied(CouponAppliedEvent event) {
        log.info("[Payments-PG] coupon.applied 수신 - orderId: {}", event.orderId());

        long requestedAmt = event.originAmt() - event.discountAmt();
        PayOrderCommand command = PayOrderCommand.of(
                event.orderId(), requestedAmt, event.payMethod(), event.payProvider(), event.installment());

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        publisher.publish(EventTopic.PAYMENT_PG_RESULT_TOPIC,
                new PaymentPgResultEvent(
                        event.orderId(), event.customerId(), event.couponId(),
                        event.items(), event.payMethod(), event.payProvider(), event.installment(),
                        event.originAmt(), event.discountAmt(),
                        PgResult.valueOf(result.status().name()), result.reason(), requestedAmt, result.pgApprovedAmt(),
                        result.pgProvider() == null ? null : result.pgProvider().name(),
                        result.responseCode(), result.pgTid(), result.pgCancelTid(),
                        result.approvedAt(), result.canceledAt(),
                        event.orderId(), LocalDateTime.now()));
        log.info("[Payments-PG] pg-result 발행 - orderId: {}, result: {}", event.orderId(), result.status());
    }
}
