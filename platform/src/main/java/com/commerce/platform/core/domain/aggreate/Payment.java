package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.PaymentId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
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
    private PayProvider payProvider;

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


    public static Payment create(PayOrderCommand command, PgProvider targetPg) {
        return Payment.builder()
                .paymentId(PaymentId.create())
                .orderId(command.getOrderId())
                .approvedAmt(command.getApprovedAmount())
                .payMethod(command.getPayMethod())
                .payProvider(command.getPayProvider())
                .installment(command.getInstallment())
                .pgProvider(targetPg)
                .paymentStatus(command.getPaymentStatus())
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public void approved(PgPayResponse pgResponse) {
        this.pgTid = pgResponse.pgTid();

        if(!pgResponse.isSuccess()) {
            this.paymentStatus = PaymentStatus.FAILED;
            return;
        }

        this.approvedAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.APPROVED;
    }

    public void canceled(PgPayCancelResponse cancelResponse) {
        this.pgCancelTid = cancelResponse.pgCcTid();

        if(!cancelResponse.isSuccess()) {
            this.paymentStatus = PaymentStatus.FAILED;
            return;
        }
        this.canceledAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.FULL_CANCELED;
    }

    /**
     * 전체/부분 취소 가능 여부 검증
     */
    public void validateForCancel() {
        if(this.paymentStatus != PaymentStatus.APPROVED) {
            throw new RuntimeException("취소가 불가능한 결제 상태입니다");
        }
    }
}
