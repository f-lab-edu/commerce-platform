package com.commerce.platform.infrastructure.adaptor;


import com.commerce.platform.core.application.port.out.CustomerCardOutPort;
import com.commerce.platform.core.domain.aggreate.CustomerCard;
import com.commerce.platform.infrastructure.persistence.CustomerCardRepository;
import com.commerce.shared.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CustomerCardAdaptor implements CustomerCardOutPort {
    private final CustomerCardRepository repository;

    @Override
    public void save(CustomerCard customerCard) {
        repository.save(customerCard);
    }

    @Override
    public int countActiveCardByCustomerId(CustomerId customerId) {
        return repository.countByActiveCustomerId(customerId);
    }

    @Override
    public Optional<CustomerCard> findActiveById(Long cardId) {
        return repository.findByIdAndActive(cardId);
    }
}
