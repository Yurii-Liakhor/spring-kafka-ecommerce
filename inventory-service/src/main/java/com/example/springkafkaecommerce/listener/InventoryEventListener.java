package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.OrderEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.OrderEventHandler;
import com.example.springkafkaecommerce.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private static final String INVENTORY_SERVICE_GROUP = "inventory-service-group";

    private final OrderEventHandler orderEventHandler;
    private final ProcessedEventService processedEventService;

    public InventoryEventListener(OrderEventHandler orderEventHandler,
                                  ProcessedEventService processedEventService) {
        this.orderEventHandler = orderEventHandler;
        this.processedEventService = processedEventService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED_TOPIC, groupId = INVENTORY_SERVICE_GROUP)
    public void consumeOrderCreated(OrderEvent orderEvent) {
        String orderUuid = orderEvent.orderUuid();
        log.debug("Consuming order created event {}", orderUuid);

        if (processedEventService.isAlreadyProcessed(orderUuid, KafkaTopics.ORDER_CREATED_TOPIC)) {
            log.warn("Duplicate event skipped orderUuid: {}", orderUuid);
            return;
        }

        orderEventHandler.handleOrderCreated(orderEvent);
    }
}