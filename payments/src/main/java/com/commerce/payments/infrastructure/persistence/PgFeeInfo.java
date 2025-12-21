package com.commerce.payments.infrastructure.persistence;

import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.shared.enums.PayProvider;
import com.commerce.payments.domain.enums.PgProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PG사별 결제방식 + 카드사/통신사 조합의 수수료율 저장
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "pg_fee_info")
public class PgFeeInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PgProvider pgProvider;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayMethod payMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayProvider payProvider;
    
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal feeRate;
    
    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDate frDt;

    @Column(nullable = false, updatable = false)
    private LocalDate toDt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public PgFeeInfo(PgProvider pgProvider, PayMethod payMethod,
                     PayProvider payProvider, BigDecimal feeRate,
                     LocalDate frDt, LocalDate toDt) {
        this.pgProvider = pgProvider;
        this.payMethod = payMethod;
        this.payProvider = payProvider;
        this.feeRate = feeRate;
        this.isActive = true;
        this.frDt = frDt;
        this.toDt = toDt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
