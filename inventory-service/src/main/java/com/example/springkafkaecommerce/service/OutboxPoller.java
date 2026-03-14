package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.OutboxEvent;
import com.example.springkafkaecommerce.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPoller(OutboxRepository outboxRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.polling-interval-ms:1000}")
    public void poll() {
        List<OutboxEvent> pending = outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pending) {
            try {
                Class<?> clazz = Class.forName(event.getEventClass());
                Object payload = objectMapper.readValue(event.getPayload(), clazz);

                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), payload).get();

                outboxRepository.markPublished(event.getId());
                log.debug("Published outbox event id={} topic={}", event.getId(), event.getTopic());

            } catch (Exception e) {
                log.error("Failed to publish outbox event id={} topic={}", event.getId(), event.getTopic(), e);
            }
        }
    }
}