package com.example.springkafkaecommerce.event;

public record ProductReservationItem(
        Long productId,
        Integer quantity
) {}
