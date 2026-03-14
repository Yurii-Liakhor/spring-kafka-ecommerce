package com.example.springkafkaecommerce.event;

public record PaymentData(
        String paymentId,
        long amount,
        String currency,
        String status,
        String paymentMethod,
        String provider,
        String createdAt
) {}
