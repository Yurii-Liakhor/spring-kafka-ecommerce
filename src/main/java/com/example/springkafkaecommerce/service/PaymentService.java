package com.example.springkafkaecommerce.service;

public interface PaymentService {
    void processPayment(String orderId);
}
