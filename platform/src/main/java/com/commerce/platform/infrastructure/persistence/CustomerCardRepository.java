package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.CustomerCard;
import com.commerce.platform.core.domain.vo.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerCardRepository extends JpaRepository<CustomerCard, Long> {

    @Query("SELECT COUNT(c) FROM CustomerCard c WHERE c.isActive = true AND c.customerId = :customerId")
    int countByActiveCustomerId(CustomerId customerId);
}
