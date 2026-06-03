package com.commerce.shared.kafka.config;

import com.commerce.shared.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Kafka 브로커 주소
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Consumer Group ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Key, Value Deserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Offset 리셋 정책: earliest - 가장 처음부터 읽기
        // latest : 가장 최근부터 가져온다.
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        
        // 자동 커밋 비활성화 (수동 커밋으로 메시지 유실 방지)
        // true: 오프셋을 주기적으로 커밋해서 관리하지 않아도 된다. (중복가능 있음)
        // false : 동기방식이다. 속도는 느리지만, 메시지 손실이 거의 말생하지 않는다. (중복가능 있음)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // 한 번에 가져올 최대 레코드 수
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        
        // Poll 타임아웃
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));

        // 재시도 무의미한 예외는 즉시 DLT.
        // BusinessException은 컨슈머에서 catch하는 게 1차이며 여기는 wrapping 안전망.
        // IllegalArgumentException/JsonProcessingException/DeserializationException은 코드/페이로드 결함 → 재시도해도 동일 실패.
        handler.addNotRetryableExceptions(
                BusinessException.class,
                IllegalArgumentException.class,
                JsonProcessingException.class,
                DeserializationException.class
        );
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // 자동 ACK 모드 (RECORD): 리스너 정상 종료 시 레코드 단위로 커밋.
        // 예외 시 ErrorHandler가 재시도 → DLT 처리 후 커밋.
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        // 동시 처리 스레드 수
        factory.setConcurrency(3);

        factory.setCommonErrorHandler(kafkaErrorHandler(kafkaTemplate));

        return factory;
    }
}
