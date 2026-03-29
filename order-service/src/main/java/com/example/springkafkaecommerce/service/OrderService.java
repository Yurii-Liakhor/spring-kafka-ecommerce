package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    String createOrder(OrderDTO order);
    void cancelOrder(String orderUuid);
    void payOrder(String orderUuid);
    void reserveOrder(String orderUuid);
    OrderDTO getOrder(String orderUuid);
    List<OrderDTO> getAllOrders();
}
