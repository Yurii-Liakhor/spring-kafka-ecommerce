package com.example.springkafkaecommerce.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(Long productId) {
        super("Not enough quantity for reservation product: " + productId);
    }
}
