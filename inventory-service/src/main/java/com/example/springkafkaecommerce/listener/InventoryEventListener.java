package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.event.OrderEvent;
import com.example.springkafkaecommerce.exception.OutOfStockException;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.InventoryService;
import com.example.springkafkaecommerce.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class InventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private static final String INVENTORY_SERVICE_GROUP = "inventory-service-group";

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private final InventoryService inventoryService;
    private final ProcessedEventService processedEventService;

    public InventoryEventListener(KafkaTemplate<String, InventoryEvent> kafkaTemplate,
                                  InventoryService inventoryService,
                                  ProcessedEventService processedEventService) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryService = inventoryService;
        this.processedEventService = processedEventService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED_TOPIC, groupId = INVENTORY_SERVICE_GROUP)
    public void consumeOrderCreated(OrderEvent orderEvent) {
        log.debug("Consuming order created event {}", orderEvent.orderUuid());

        if (!processedEventService.markAsProcessed(orderEvent.orderUuid(), KafkaTopics.ORDER_CREATED_TOPIC)) {
            log.warn("Duplicate event skipped orderUuid: {}", orderEvent.orderUuid());
            return;
        }

        try {
            inventoryService.reserveProducts(orderEvent.reserveProducts());

            kafkaTemplate.send(
                    KafkaTopics.INVENTORY_RESERVED_TOPIC,
                    orderEvent.orderUuid(),
                    new InventoryEvent(orderEvent.orderUuid(), orderEvent.reserveProducts())
            );
        } catch (OutOfStockException e) {
            kafkaTemplate.send(
                    KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC,
                    orderEvent.orderUuid(),
                    new InventoryEvent(orderEvent.orderUuid(), Collections.emptyList())
            );
        } catch (Exception e) {
            log.error("Unexpected error processing order {}, rolling back processed event", orderEvent.orderUuid(), e);
            processedEventService.deleteProcessedEvent(orderEvent.orderUuid(), KafkaTopics.ORDER_CREATED_TOPIC);
            throw e;
        }
    }
}