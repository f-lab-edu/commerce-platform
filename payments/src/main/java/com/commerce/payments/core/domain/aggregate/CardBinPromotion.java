package com.commerce.payments.core.domain.aggregate;

import com.commerce.shared.enums.PayProvider;
import com.commerce.payments.core.domain.vo.promotion.BasePromotionData;
import com.commerce.payments.infrastructure.persistence.converter.PromotionDataConverter;
import com.commerce.shared.vo.ValidPeriod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "card_bin_promotion")
@Entity
public class CardBinPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_bin", nullable = false)
    private String cardBin;

    @Column(name = "card_name", nullable = false)
    private String cardName;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_provider", nullable = false)
    private PayProvider payProvider;

    /**
     * JSON 프로모션 데이터
     * - Converter에서 임시 타입으로 변환
     * - 조회 후 PromotionDataPostProcessor에서 PayProvider에 맞게 재변환
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "promotion_data", nullable = false, columnDefinition = "json")
    @Convert(converter = PromotionDataConverter.class)
    private BasePromotionData promotionData;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Embedded
    private ValidPeriod validPeriod;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public CardBinPromotion(
        String cardBin,
        String cardName,
        PayProvider payProvider,
        BasePromotionData promotionData,
        ValidPeriod validPeriod
    ) {
        this.cardBin = cardBin;
        this.cardName = cardName;
        this.payProvider = payProvider;
        this.promotionData = promotionData;
        this.validPeriod = validPeriod;
        this.isActive = true;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
}
