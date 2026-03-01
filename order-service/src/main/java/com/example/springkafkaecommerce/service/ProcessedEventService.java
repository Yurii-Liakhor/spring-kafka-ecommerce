package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.ProcessedEvent;
import com.example.springkafkaecommerce.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    public ProcessedEventService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void markAsProcessed(String eventId, String topic) {
        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .topic(topic)
                        .processedAt(Instant.now())
                        .build()
        );
    }

    public boolean isAlreadyProcessed(String eventId, String topic) {
        return processedEventRepository.existsByEventIdAndTopic(eventId, topic);
    }
}