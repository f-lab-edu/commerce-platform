package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PgFeeInfoRepository extends JpaRepository<PgFeeInfo, Long> {
    
    /**
     * 결제방식 + 카드사/통신사로 활성화된 PG 수수료 조회
     * 수수료 asc
     */
    @Query("""
            SELECT p FROM PgFeeInfo p
            WHERE p.payMethod = :payMethod
                AND p.payProvider = :payProvider
                AND p.isActive = true
                AND NOW() between p.frDt AND p.toDt
            ORDER BY p.feeRate ASC
            """)
    List<PgFeeInfo> findByPayMethodAndPayProvider(
        @Param("payMethod") PayMethod payMethod,
        @Param("payProvider") PayProvider payProvider
    );

}
