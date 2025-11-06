package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.CustomerCard;
import com.commerce.platform.core.domain.vo.CustomerId;

public interface CustomerCardOutPort {
    void save(CustomerCard customerCard);
    int countActiveCardByCustomerId(CustomerId customerId);
}
