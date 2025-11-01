package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.PaymentId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment")
@Entity
public class Payment {

    @EmbeddedId
    private PaymentId paymentId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "order_id", nullable = false))
    private OrderId orderId;

    // 결제 정보
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "approved_amt", nullable = false))
    private Money approvedAmt; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_method", nullable = false, length = 10)
    private PayMethod payMethod;

    @Column(name = "pay_provider", length = 10)
    private PayProvider cardProvider;

    @Column(name = "installment", length = 2)
    private String installment;

    // PG 정보
    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false, length = 20)
    private PgProvider pgProvider;

    @Column(name = "pg_tid", length = 100)
    private String pgTid;  // PG사 승인 ID

    @Column(name = "pg_cancel_tid", length = 100)
    private String pgCancelTid;  // PG사 전체취소 ID

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

}
