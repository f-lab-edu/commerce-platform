package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.application.in.dto.RegistryCardCommand;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.shared.service.AesCryptoFacade;
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
    private Long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "customer_id", nullable = false))
    private CustomerId customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_provider", nullable = false, length = 10)
    private PayProvider cardProvider;

    @Column(name = "card_number_masked", nullable = false, length = 20)
    private String cardNumberMasked;

    @Column(name = "card_number_enc", nullable = false, unique = true, length = 64)
    private String cardNumberEnc;

    @Column(name = "pass_word_enc", nullable = false, length = 40)
    private String passwordEnc; // 2자리

    @Column(name = "expiry_month_enc", nullable = false, length = 40)
    private String expiryMonthEnc; // 2자리

    @Column(name = "expiry_year_enc", nullable = false, length = 40)
    private String expiryYearEnc; // 2자리

    @Column(name = "birth_date_enc", nullable = false, length = 48)
    private String birthDateEnc; // 6자리

    @Column(name = "card_nickname", unique = true, length = 20)
    private String cardNickname;  // 사용자가 지정한 카드 별칭

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;  // 활성화 여부

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static CustomerCard create(RegistryCardCommand command,
                                      AesCryptoFacade aesCryptoFacade) {
        return new CustomerCard(
                command.customerId(),
                command.payProvider(),
                "***",
                aesCryptoFacade.encrypt(command.cardNumber()),
                aesCryptoFacade.encrypt(command.password()),
                aesCryptoFacade.encrypt(command.expiryMonth()),
                aesCryptoFacade.encrypt(command.expiryYear()),
                aesCryptoFacade.encrypt(command.birthDate()),
                command.cardNickName(),
                true,
                LocalDateTime.now(),
                null
        );
    }

    private CustomerCard(CustomerId customerId, PayProvider cardProvider,
                         String cardNumberMasked, String cardNumberEnc,
                         String passwordEnc, String expiryMonthEnc,
                         String expiryYearEnc, String birthDateEnc,
                         String cardNickname, Boolean isActive,
                         LocalDateTime registeredAt, LocalDateTime deletedAt) {
        this.customerId = customerId;
        this.cardProvider = cardProvider;
        this.cardNumberMasked = cardNumberMasked;
        this.cardNumberEnc = cardNumberEnc;
        this.passwordEnc = passwordEnc;
        this.expiryMonthEnc = expiryMonthEnc;
        this.expiryYearEnc = expiryYearEnc;
        this.birthDateEnc = birthDateEnc;
        this.cardNickname = cardNickname;
        this.isActive = isActive;
        this.registeredAt = registeredAt;
        this.deletedAt = deletedAt;
    }
}