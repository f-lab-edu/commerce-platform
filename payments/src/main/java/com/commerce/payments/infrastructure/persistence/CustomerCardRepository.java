package com.commerce.payments.infrastructure.persistence;

import com.commerce.payments.domain.aggregate.CustomerCard;
import com.commerce.shared.vo.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerCardRepository extends JpaRepository<CustomerCard, Long> {

    @Query("SELECT COUNT(c) FROM CustomerCard c WHERE c.customerId = :customerId AND c.isActive = true")
    int countByActiveCustomerId(CustomerId customerId);

    @Query("SELECT c FROM CustomerCard c WHERE c.id = :cardId AND c.isActive = true")
    Optional<CustomerCard> findByIdAndActive(Long cardId);

}
