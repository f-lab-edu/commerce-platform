package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.PaymentId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_part_cancel")
@Entity
public class PaymentPartCancel {

    @EmbeddedId
    private PaymentId paymentPartCancelId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "approved_payment_id", nullable = false))
    private PaymentId approvedPaymentId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "canceled_amt"))
    private Money canceledAmt;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "remain_amt"))
    private Money remainAmt;

    @Column(name = "pg_cancel_tid", length = 100)
    private String pgCancelTid;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
}
