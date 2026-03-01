package com.example.springkafkaecommerce.model;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    PAYMENT_PROCESSING,
    CONFIRMED,
    CANCELLED,
    SHIPPED
}
