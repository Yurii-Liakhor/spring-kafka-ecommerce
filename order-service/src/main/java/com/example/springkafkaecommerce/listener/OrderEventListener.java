package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.service.OrderService;
import com.example.springkafkaecommerce.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private static final String ORDER_SERVICE_GROUP = "order-service-group";

    private final OrderService orderService;
    private final ProcessedEventService processedEventService;

    public OrderEventListener(OrderService orderService,
                              ProcessedEventService processedEventService) {
        this.orderService = orderService;
        this.processedEventService = processedEventService;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeProductReserved(InventoryEvent inventoryEvent) {
        String orderUuid = inventoryEvent.orderUuid();
        log.debug("Consuming inventory reserved event. Order UUID: {}", orderUuid);

        if (!processedEventService.markAsProcessed(orderUuid, KafkaTopics.INVENTORY_RESERVED_TOPIC)) {
            log.warn("Duplicate event skipped. Order UUID: {}", orderUuid);
            return;
        }

        orderService.reserveOrder(orderUuid);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeProductOutOfStock(InventoryEvent inventoryEvent) {
        String orderUuid = inventoryEvent.orderUuid();
        log.debug("Consuming inventory out of stock event. Order UUID: {}", orderUuid);

        if (!processedEventService.markAsProcessed(inventoryEvent.orderUuid(), KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC)) {
            log.warn("Duplicate event skipped. Order UUID: {}", orderUuid);
            return;
        }

        orderService.cancelOrder(orderUuid);
    }
}
