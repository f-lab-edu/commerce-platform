package com.commerce.shared.kafka;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * DB 트랜잭션 커밋 후에만 Kafka 이벤트를 발행한다.
 * - 트랜잭션 활성 시: afterCommit 콜백으로 등록 -> 롤백 시 발행하지 않음
 * - 트랜잭션 비활성 시: 즉시 발행 (Consumer에서 이벤트 발행 시)
 */
@RequiredArgsConstructor
@Component
public class TransactionalEventPublisher {

    private final KafkaEventPublisher kafkaEventPublisher;

    public <T extends DomainEvent> void publish(EventTopic topic, T event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        kafkaEventPublisher.publish(topic, event);
                    }
                }
            );
        } else {
            kafkaEventPublisher.publish(topic, event);
        }
    }
}
