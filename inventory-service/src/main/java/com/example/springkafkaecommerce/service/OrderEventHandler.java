package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.OutboxEvent;
import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.event.OrderEvent;
import com.example.springkafkaecommerce.exception.OutOfStockException;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
public class OrderEventHandler {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;

    public OrderEventHandler(OutboxRepository outboxRepository,
                             ObjectMapper objectMapper,
                             InventoryService inventoryService) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
    }

    // Немає @Transactional — reserveProducts використовує REQUIRES_NEW і комітить самостійно.
    // Збереження outbox відбувається одразу після, у власній Spring Data JPA транзакції.
    public void handleOrderCreated(OrderEvent orderEvent) {
        String topic;
        InventoryEvent outEvent;

        try {
            inventoryService.reserveProducts(orderEvent.orderUuid(), orderEvent.reserveProducts());
            topic = KafkaTopics.INVENTORY_RESERVED_TOPIC;
            outEvent = new InventoryEvent(orderEvent.orderUuid(), orderEvent.reserveProducts(), orderEvent.paymentData());
        } catch (OutOfStockException e) {
            topic = KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC;
            outEvent = new InventoryEvent(orderEvent.orderUuid(), Collections.emptyList(), orderEvent.paymentData());
        }

        try {
            outboxRepository.save(OutboxEvent.builder()
                    .aggregateId(orderEvent.orderUuid())
                    .topic(topic)
                    .payload(objectMapper.writeValueAsString(outEvent))
                    .eventClass(InventoryEvent.class.getName())
                    .createdAt(Instant.now())
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize InventoryEvent for orderUuid: " + orderEvent.orderUuid(), e);
        }
    }
}