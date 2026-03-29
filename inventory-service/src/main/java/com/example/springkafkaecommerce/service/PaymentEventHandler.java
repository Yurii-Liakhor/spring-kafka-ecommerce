package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.event.PaymentEvent;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventHandler {

    private final InventoryService inventoryService;

    public PaymentEventHandler(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void handleNotPaid(PaymentEvent paymentEvent) {
        inventoryService.releaseProducts(paymentEvent.orderUuid(), paymentEvent.reserveProducts());
    }
}
