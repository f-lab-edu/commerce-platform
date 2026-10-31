package com.commerce.payments.core.domain.aggregate;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;      // pg사 응답코드

    @Column(name = "error_message", length = 500)
    private String errorMessage;   // pg사 응답메시지

    /**
     * 거래실패 생성
     */
    public static PaymentFailure create(String errorCode, String errorMessage) {
        PaymentFailure failure = new PaymentFailure();
        failure.errorCode = errorCode;
        failure.errorMessage = errorMessage;
        failure.createdAt = LocalDateTime.now();
        failure.updatedAt = failure.createdAt;
        return failure;
    }
}
