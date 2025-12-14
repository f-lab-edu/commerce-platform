package com.commerce.payments.application.port.in;

import com.commerce.payments.domain.enums.PayProvider;
import com.commerce.payments.infrastructure.persistence.CustomerCardRepository;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.service.AesCryptoFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.commerce.shared.exception.BusinessError.DUPLICATED_REGISTRY_CARD;
import static com.commerce.shared.exception.BusinessError.EXCEED_REGISTRY_CARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CustomerUseCaseImplTest {
    @Autowired
    CustomerUseCase customerUseCase;

    @Autowired
    CustomerCardRepository customerCardRepository;

    @Autowired
    AesCryptoFacade aesCryptoFacade;

    @DisplayName("중복 카드등록 실패")
    @Test
    void registryPayCard_duplicateError() {
        RegistryCardCommand command = new RegistryCardCommand(
                CustomerId.of("test1"),
                PayProvider.KB,
                "11111111113333333",
                "12",
                "08",
                "25",
                "990111",
                "테스트카드등록"
        );

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> {
                    customerUseCase.registryPayCard(command);
                    customerUseCase.registryPayCard(command);
                }
        );

        assertThat(ex.getMessage())
                .isEqualTo(DUPLICATED_REGISTRY_CARD.getMessage());
    }

    @Transactional
    @DisplayName("카드 등록 5개까지 가능")
    @Test
    void registryPayCard_max5() {
        List<RegistryCardCommand> commands = new ArrayList<>();
        CustomerId customerId = CustomerId.of("test1");
        PayProvider[] providers = PayProvider.values();

        for (int i = 0; i < 6; i++) {
            commands.add(new RegistryCardCommand(
                    customerId,
                    providers[i],
                    "11111111113333333" + i,
                    "12",
                    "08",
                    "25",
                    "990111",
                    "테스트카드등록" + i
            ));
        }

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> {
                    for(RegistryCardCommand command : commands) {
                        customerUseCase.registryPayCard(command);
                    }
                }
        );

        assertThat(ex.getMessage())
                .isEqualTo(EXCEED_REGISTRY_CARD.getMessage());
    }
}