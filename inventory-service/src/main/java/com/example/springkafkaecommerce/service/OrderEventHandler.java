package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.event.OrderEvent;
import com.example.springkafkaecommerce.exception.OutOfStockException;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
public class OrderEventHandler {

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private final InventoryService inventoryService;
    private final ProcessedEventService processedEventService;

    public OrderEventHandler(KafkaTemplate<String, InventoryEvent> kafkaTemplate, InventoryService inventoryService, ProcessedEventService processedEventService) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryService = inventoryService;
        this.processedEventService = processedEventService;
    }

    @Transactional
    public void handleOrderCreated(OrderEvent orderEvent) {
        try {
            inventoryService.reserveProducts(orderEvent.reserveProducts());
            sendAndWait(KafkaTopics.INVENTORY_RESERVED_TOPIC, orderEvent.orderUuid(),
                    new InventoryEvent(orderEvent.orderUuid(), orderEvent.reserveProducts()));
        } catch (OutOfStockException e) {
            sendAndWait(KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC, orderEvent.orderUuid(),
                    new InventoryEvent(orderEvent.orderUuid(), Collections.emptyList()));
        }
        processedEventService.markAsProcessed(orderEvent.orderUuid(), KafkaTopics.ORDER_CREATED_TOPIC);
    }

    private void sendAndWait(String topic, String key, InventoryEvent event) {
        try {
            kafkaTemplate.send(topic, key, event).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka send interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
