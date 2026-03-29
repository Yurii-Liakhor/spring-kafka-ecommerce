package com.example.springkafkaecommerce.service.handler;

import com.example.springkafkaecommerce.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryEventHandler {

    private final OrderService orderService;

    public InventoryEventHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Transactional
    public void handleReserved(String orderUuid) {
        orderService.reserveOrder(orderUuid);
    }

    @Transactional
    public void handleOutOfStock(String orderUuid) {
        orderService.cancelOrder(orderUuid);
    }
}