package com.commerce.shared.kafka;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionalEventPublisherTest {

    @Mock
    KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    TransactionalEventPublisher transactionalEventPublisher;

    @DisplayName("트랜잭션 활성 시 afterCommit 콜백으로 등록한다")
    @Test
    void registerAfterCommitWhenTransactionActive() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            DomainEvent event = new TestEvent("key1", LocalDateTime.now());
            transactionalEventPublisher.publish(EventTopic.ORDER_CREATED_TOPIC, event);
            verify(kafkaEventPublisher, never()).publish(any(), any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @DisplayName("트랜잭션 롤백 시 이벤트가 발행되지 않는다")
    @Test
    void doesNotPublishWhenTransactionRollsBack() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            DomainEvent event = new TestEvent("key1", LocalDateTime.now());
            transactionalEventPublisher.publish(EventTopic.ORDER_CREATED_TOPIC, event);

            TransactionSynchronizationManager.getSynchronizations().forEach(
                sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK)
            );

            verify(kafkaEventPublisher, never()).publish(any(), any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    record TestEvent(String key, LocalDateTime timestamp) implements DomainEvent {}
}
