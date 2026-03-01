package com.example.springkafkaecommerce.controller;

import com.example.springkafkaecommerce.dto.OrderDTO;
import com.example.springkafkaecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        log.debug("Getting all orders");

        return orderService.getAllOrders();
    }

    @GetMapping("/{orderUuid}")
    public OrderDTO getOrderById(@PathVariable String orderUuid) {
        log.debug("Getting order {}", orderUuid);

        return orderService.getOrder(orderUuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> createOrder(@RequestBody OrderDTO order) {
        log.debug("Creating new order");

        String uuid = orderService.createOrder(order);
        return Map.of("orderUuid", uuid);
    }
}
