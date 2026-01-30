package com.commerce.payments.infrastructure.persistence;

import com.commerce.payments.core.domain.aggregate.CardBinPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardBinPromotionRepository extends JpaRepository<CardBinPromotion, Long> {
    
    /**
     * 활성화된 카드 BIN 조회
     */
    @Query("""
            SELECT c FROM CardBinPromotion c
            WHERE c.isActive = true
            AND CURRENT_DATE BETWEEN c.validPeriod.frDt AND c.validPeriod.toDt
            """)
    List<CardBinPromotion> findAllByActive();

}
