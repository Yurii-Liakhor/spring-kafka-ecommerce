package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.kafka.KafkaTopics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryEventHandler {

    private final OrderService orderService;
    private final ProcessedEventService processedEventService;

    public InventoryEventHandler(OrderService orderService,
                                 ProcessedEventService processedEventService) {
        this.orderService = orderService;
        this.processedEventService = processedEventService;
    }

//    @Transactional
//    public void handleReserved(String orderUuid) {
//        orderService.reserveOrder(orderUuid);
//        processedEventService.markAsProcessed(orderUuid, KafkaTopics.INVENTORY_RESERVED_TOPIC);
//    }

    @Transactional
    public void handleOutOfStock(String orderUuid) {
        orderService.cancelOrder(orderUuid);
        processedEventService.markAsProcessed(orderUuid, KafkaTopics.INVENTORY_OUT_OF_STOCK_TOPIC);
    }
}