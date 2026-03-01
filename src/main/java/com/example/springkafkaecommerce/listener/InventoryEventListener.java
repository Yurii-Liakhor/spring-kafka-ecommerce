package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.entity.ProcessedEvent;
import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.event.OrderEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.repository.ProcessedEventRepository;
import com.example.springkafkaecommerce.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;

@Component
public class InventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private static final String INVENTORY_SERVICE_GROUP = "inventory-service-group";

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;

    public InventoryEventListener(KafkaTemplate<String, InventoryEvent> kafkaTemplate,
                                  InventoryService inventoryService,
                                  ProcessedEventRepository processedEventRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryService = inventoryService;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED_TOPIC, groupId = INVENTORY_SERVICE_GROUP)
    public void consumeOrderCreated(OrderEvent orderEvent) {
        log.debug("Consuming order created event {}", orderEvent.orderUuid());

        if (!markAsProcessed(orderEvent.orderUuid(), KafkaTopics.ORDER_CREATED_TOPIC)) {
            log.warn("Duplicate event skipped orderUuid: {}", orderEvent.orderUuid());
            return;
        }

        try {
            inventoryService.reserveProducts(orderEvent.orderUuid(), orderEvent.reserveProducts());

            kafkaTemplate.send(
                    KafkaTopics.INVENTORY_RESERVED_TOPIC,
                    orderEvent.orderUuid(),
                    new InventoryEvent(orderEvent.orderUuid(), orderEvent.reserveProducts())
            );
        } catch (IllegalStateException e) {
            kafkaTemplate.send(
                    KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC,
                    orderEvent.orderUuid(),
                    new InventoryEvent(orderEvent.orderUuid(), Collections.emptyList())
            );
        }
    }

    private boolean markAsProcessed(String eventId, String topic) {
        try {
            processedEventRepository.saveAndFlush(ProcessedEvent.builder()
                    .eventId(eventId)
                    .topic(topic)
                    .processedAt(Instant.now())
                    .build());
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
