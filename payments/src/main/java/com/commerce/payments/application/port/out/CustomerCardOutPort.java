package com.commerce.payments.application.port.out;

import com.commerce.payments.domain.aggregate.CustomerCard;
import com.commerce.shared.vo.CustomerId;

import java.util.Optional;

public interface CustomerCardOutPort {
    void save(CustomerCard customerCard);
    int countActiveCardByCustomerId(CustomerId customerId);
    Optional<CustomerCard> findActiveById(Long cardId);
}
