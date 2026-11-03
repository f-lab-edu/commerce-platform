package com.commerce.payments.core.domain.aggregate;

import com.commerce.payments.core.domain.enums.PgApprovalStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.shared.vo.OrderId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 거래실패 내역
 * - 승인 실패 거래는 payment 테이블이 아닌 이 테이블에 저장한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_failure")
@Entity
public class PaymentFailure {

    private static final int ERROR_MESSAGE_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "order_id", nullable = false, length = OrderId.LENGTH))
    private OrderId orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fail_type", nullable = false, length = 30)
    private PgApprovalStatus failType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", length = 20)
    private PgProvider pgProvider;

    @Column(name = "pg_tid", length = 100)
    private String pgTid;          // pg 승인Tid (망취소 실패 시 수동 취소 대상)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;      // pg사 응답코드

    @Column(name = "error_message", length = ERROR_MESSAGE_LENGTH)
    private String errorMessage;   // pg사 응답메시지 / 실패사유

    /**
     * 거래실패 생성
     */
    public static PaymentFailure create(OrderId orderId, PgApprovalResult result) {
        PaymentFailure failure = new PaymentFailure();
        failure.orderId = orderId;
        failure.failType = result.status();
        failure.pgProvider = result.pgProvider();
        failure.pgTid = result.pgTid();
        failure.errorCode = result.responseCode();
        failure.errorMessage = truncate(result.reason());
        failure.createdAt = LocalDateTime.now();
        failure.updatedAt = failure.createdAt;
        return failure;
    }

    private static String truncate(String message) {
        if (message == null || message.length() <= ERROR_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, ERROR_MESSAGE_LENGTH);
    }
}
