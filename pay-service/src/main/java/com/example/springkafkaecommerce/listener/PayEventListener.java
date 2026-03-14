package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.InventoryEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PayEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayEventListener.class);
    private static final String PAY_SERVICE_GROUP = "pay-service-group";

    private final InventoryEventHandler inventoryEventHandler;

    public PayEventListener(InventoryEventHandler inventoryEventHandler) {
        this.inventoryEventHandler = inventoryEventHandler;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED_TOPIC, groupId = PAY_SERVICE_GROUP)
    public void consumeProductReserved(InventoryEvent inventoryEvent) {
        log.debug("Consuming inventory reserved event. orderUuid: {}", inventoryEvent.orderUuid());
        inventoryEventHandler.handlePayment(inventoryEvent);
    }
}