package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.vo.CustomerId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "customer_card")
@Entity
public class CustomerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "customer_id", nullable = false))
    private CustomerId customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_provider", nullable = false, length = 10)
    private PayProvider cardProvider;

    @Column(name = "card_number_masked", nullable = false, length = 20)
    private String cardNumberMasked;

    @Column(name = "card_number_enc", nullable = false, length = 20)
    private String cardNumberEnc;

    @Column(name = "pass_word_enc", nullable = false, length = 2)
    private String passwordEnc;

    @Column(name = "expiry_month_enc", nullable = false, length = 2)
    private String expiryMonthEnc;

    @Column(name = "expiry_year_enc", nullable = false, length = 2)
    private String expiryYearEnc;

    @Column(name = "birth_date_enc", nullable = false, length = 6)
    private String expireMmYyEnc;

    @Column(name = "card_nickname", length = 20)
    private String cardNickname;  // 사용자가 지정한 카드 별칭

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;  // 기본 결제 수단 여부

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;  // 활성화 여부

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}