package com.commerce.inventory.core.infrastructure.adaptor;

import com.commerce.inventory.core.application.port.out.ProcessedEventPort;
import com.commerce.inventory.core.infrastructure.persistence.ProcessedEvent;
import com.commerce.inventory.core.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * processed_events 테이블 기반 멱등 처리 포트 어댑터.
 */
@Component
@RequiredArgsConstructor
public class ProcessedEventAdaptor implements ProcessedEventPort {

    private final ProcessedEventRepository processedEventRepository;

    @Override
    public boolean exists(String eventId) {
        return processedEventRepository.existsById(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        processedEventRepository.save(new ProcessedEvent(eventId));
    }
}
