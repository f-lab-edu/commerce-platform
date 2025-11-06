package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.RegistryCardCommand;
import com.commerce.platform.core.application.out.CustomerCardOutPort;
import com.commerce.platform.core.domain.aggreate.CustomerCard;
import com.commerce.platform.shared.exception.BusinessException;
import com.commerce.platform.shared.service.AesCryptoFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import static com.commerce.platform.shared.exception.BusinessError.DUPLICATED_REGISTRY_CARD;
import static com.commerce.platform.shared.exception.BusinessError.EXCEED_REGISTRY_CARD;

@Log4j2
@RequiredArgsConstructor
@Service
public class CustomerUseCaseImpl implements CustomerUseCase{
    private final AesCryptoFacade aesCryptoFacade;
    private final CustomerCardOutPort customerCardOutPort;

    @Override
    public void registryPayCard(RegistryCardCommand command) {
        try {
            // 최대 5개 등록 가능 검증
            int cardCount = customerCardOutPort.countActiveCardByCustomerId(command.customerId());
            if(cardCount == 5) throw new BusinessException(EXCEED_REGISTRY_CARD);

            CustomerCard customerCard = CustomerCard.create(command, aesCryptoFacade);
            customerCardOutPort.save(customerCard);
        } catch (DataIntegrityViolationException e) {
            log.error("중복 카드 등록 실패");
            throw new BusinessException(DUPLICATED_REGISTRY_CARD);
        }
    }
}
