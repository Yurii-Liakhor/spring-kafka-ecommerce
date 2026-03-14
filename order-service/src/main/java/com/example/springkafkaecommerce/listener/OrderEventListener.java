package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.event.PaymentEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.InventoryEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private static final String ORDER_SERVICE_GROUP = "order-service-group";

    private final InventoryEventHandler inventoryEventHandler;

    public OrderEventListener(InventoryEventHandler inventoryEventHandler) {
        this.inventoryEventHandler = inventoryEventHandler;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_PAID_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeOrderPayed(PaymentEvent paymentEvent) {
        log.debug("Consuming payment event orderUuid: {}", paymentEvent.orderUuid());
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeProductOutOfStock(InventoryEvent inventoryEvent) {
        String orderUuid = inventoryEvent.orderUuid();
        log.debug("Consuming inventory out of stock event orderUuid: {}", orderUuid);
        inventoryEventHandler.handleOutOfStock(orderUuid);
    }
}