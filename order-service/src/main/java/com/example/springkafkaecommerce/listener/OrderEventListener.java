package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.event.PaymentEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.handler.InventoryEventHandler;
import com.example.springkafkaecommerce.service.handler.PaymentEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private static final String ORDER_SERVICE_GROUP = "order-service-group";

    private final InventoryEventHandler inventoryEventHandler;
    private final PaymentEventHandler paymentEventHandler;

    public OrderEventListener(InventoryEventHandler inventoryEventHandler,
                              PaymentEventHandler paymentEventHandler) {
        this.inventoryEventHandler = inventoryEventHandler;
        this.paymentEventHandler = paymentEventHandler;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeProductReserved(InventoryEvent inventoryEvent) {
        String orderUuid = inventoryEvent.orderUuid();
        log.debug("Consuming inventory reserved event orderUuid: {}", orderUuid);
        inventoryEventHandler.handleReserved(orderUuid);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeProductOutOfStock(InventoryEvent inventoryEvent) {
        String orderUuid = inventoryEvent.orderUuid();
        log.debug("Consuming inventory out of stock event orderUuid: {}", orderUuid);
        inventoryEventHandler.handleOutOfStock(orderUuid);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_PAID_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeOrderPaid(PaymentEvent paymentEvent) {
        String orderUuid = paymentEvent.orderUuid();
        log.debug("Consuming paid event orderUuid: {}", orderUuid);
        paymentEventHandler.handlePaid(orderUuid);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_NOT_PAID_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeOrderNotPaid(PaymentEvent paymentEvent) {
        String orderUuid = paymentEvent.orderUuid();
        log.debug("Consuming not paid event orderUuid: {}", orderUuid);
        paymentEventHandler.handleNotPaid(orderUuid);
    }
}