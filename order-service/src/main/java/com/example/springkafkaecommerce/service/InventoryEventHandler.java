package com.example.springkafkaecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryEventHandler {

    private final OrderService orderService;

    public InventoryEventHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Transactional
    public void handleOutOfStock(String orderUuid) {
        orderService.cancelOrder(orderUuid);
    }
}