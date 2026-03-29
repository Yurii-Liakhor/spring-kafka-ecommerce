package com.example.springkafkaecommerce.service.handler;

import com.example.springkafkaecommerce.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentEventHandler {

    private final OrderService orderService;

    public PaymentEventHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Transactional
    public void handlePaid(String orderUuid) {
        orderService.payOrder(orderUuid);
    }

    @Transactional
    public void handleNotPaid(String orderUuid) {
        orderService.cancelOrder(orderUuid);
    }
}
