package com.commerce.payments.infrastructure.persistence;

import com.commerce.payments.domain.aggregate.PaymentPartCancel;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentPartCancelRepository extends JpaRepository<PaymentPartCancel, Long> {

    boolean existsPaymentPartCancelByApprovedPaymentId(PaymentId paymentId);

    /**
     * 최신 남은 금액 조회
     */
    @Query("SELECT p.remainAmt FROM PaymentPartCancel p WHERE p.approvedPaymentId = :paymentId ORDER BY p.id DESC LIMIT 1")
    Money selectRemainAmountByPaymentId(@Param("payment") PaymentId paymentId);
}
