package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.PaymentPartCancel;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.PaymentId;
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
