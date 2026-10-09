package com.commerce.shared.kafka;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.kafka.event.type.CancelType;
import com.commerce.shared.kafka.event.type.EventHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Map;

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
            verify(kafkaEventPublisher, never()).publish(any(), any(), anyMap());
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

            verify(kafkaEventPublisher, never()).publish(any(), any(), anyMap());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @DisplayName("트랜잭션 커밋 후 헤더와 함께 발행한다")
    @Test
    void publishesWithHeadersAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            DomainEvent event = new TestEvent("key1", LocalDateTime.now());
            Map<String, String> headers = Map.of(EventHeaders.CANCEL_TYPE, CancelType.VOID.name());
            transactionalEventPublisher.publish(EventTopic.PAYMENT_PG_CANCEL_TOPIC, event, headers);

            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

            verify(kafkaEventPublisher).publish(EventTopic.PAYMENT_PG_CANCEL_TOPIC, event, headers);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @DisplayName("트랜잭션이 없으면 헤더 없이 즉시 발행한다")
    @Test
    void publishesImmediatelyWithoutTransaction() {
        DomainEvent event = new TestEvent("key1", LocalDateTime.now());
        transactionalEventPublisher.publish(EventTopic.ORDER_CREATED_TOPIC, event);

        verify(kafkaEventPublisher).publish(EventTopic.ORDER_CREATED_TOPIC, event, Map.of());
    }

    record TestEvent(String key, LocalDateTime timestamp) implements DomainEvent {}
}
