package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.OrderEvent;
import com.example.springkafkaecommerce.event.PaymentEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.OrderEventHandler;
import com.example.springkafkaecommerce.service.PaymentEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private static final String INVENTORY_SERVICE_GROUP = "inventory-service-group";

    private final OrderEventHandler orderEventHandler;
    private final PaymentEventHandler paymentEventHandler;

    public InventoryEventListener(OrderEventHandler orderEventHandler,
                                  PaymentEventHandler paymentEventHandler) {
        this.orderEventHandler = orderEventHandler;
        this.paymentEventHandler = paymentEventHandler;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED_TOPIC, groupId = INVENTORY_SERVICE_GROUP)
    public void consumeOrderCreated(OrderEvent orderEvent) {
        log.debug("Consuming order created event {}", orderEvent.orderUuid());
        orderEventHandler.handleOrderCreated(orderEvent);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_NOT_PAID_TOPIC, groupId = INVENTORY_SERVICE_GROUP)
    public void consumePaymentNotPaid(PaymentEvent paymentEvent) {
        log.debug("Consuming payment not paid event {}", paymentEvent.orderUuid());
        paymentEventHandler.handleNotPaid(paymentEvent);
    }
}