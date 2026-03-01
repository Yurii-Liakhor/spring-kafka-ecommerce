package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.InventoryEventHandler;
import com.example.springkafkaecommerce.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private static final String ORDER_SERVICE_GROUP = "order-service-group";

    private final InventoryEventHandler inventoryEventHandler;
    private final ProcessedEventService processedEventService;

    public OrderEventListener(InventoryEventHandler inventoryEventHandler,
                              ProcessedEventService processedEventService) {
        this.inventoryEventHandler = inventoryEventHandler;
        this.processedEventService = processedEventService;
    }

//    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED_TOPIC, groupId = ORDER_SERVICE_GROUP)
//    public void consumeProductReserved(InventoryEvent inventoryEvent) {
//        String orderUuid = inventoryEvent.orderUuid();
//        log.debug("Consuming inventory reserved event. Order UUID: {}", orderUuid);
//
//        if (processedEventService.isAlreadyProcessed(orderUuid, KafkaTopics.INVENTORY_RESERVED_TOPIC)) {
//            log.warn("Duplicate event skipped orderUuid: {}", orderUuid);
//            return;
//        }
//
//        inventoryEventHandler.handleReserved(orderUuid);
//    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeProductOutOfStock(InventoryEvent inventoryEvent) {
        String orderUuid = inventoryEvent.orderUuid();
        log.debug("Consuming inventory out of stock event orderUuid: {}", orderUuid);

        if (processedEventService.isAlreadyProcessed(orderUuid, KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC)) {
            log.warn("Duplicate event skipped orderUuid: {}", orderUuid);
            return;
        }

        inventoryEventHandler.handleOutOfStock(orderUuid);
    }
}