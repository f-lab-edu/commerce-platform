package com.commerce.payments.core.domain.aggregate;

import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.PaymentId;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "part_canceled_payment_id", nullable = false))
    })
    private PaymentId paymentPartCancelId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "approved_payment_id", nullable = false))
    })
    private PaymentId approvedPaymentId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "canceled_amt", nullable = false))
    })
    private Money canceledAmt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "remain_amt", nullable = false))
    })
    private Money remainAmt;

    @Column(name = "pg_cancel_tid", length = 100)
    private String pgCancelTid;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /**
     * 부분 취소 생성
     */
    public static PaymentPartCancel create(
            PaymentId approvedPaymentId,
            Money canceledAmt,
            Money remainAmt
    ) {
        PaymentPartCancel partCancel = new PaymentPartCancel();
        partCancel.paymentPartCancelId = PaymentId.create();
        partCancel.approvedPaymentId = approvedPaymentId;
        partCancel.canceledAmt = canceledAmt;
        partCancel.remainAmt = remainAmt;
        partCancel.requestedAt = LocalDateTime.now();
        return partCancel;
    }

    /**부분취소 완료 처리*/
    public void completed(String pgCancelTid) {
        this.pgCancelTid = pgCancelTid;
        this.canceledAt = LocalDateTime.now();
    }
}
