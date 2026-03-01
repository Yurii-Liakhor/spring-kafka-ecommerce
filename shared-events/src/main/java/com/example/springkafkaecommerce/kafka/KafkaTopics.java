package com.example.springkafkaecommerce.kafka;

public class KafkaTopics {
    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_CANCELED_TOPIC = "order.canceled";
    public static final String INVENTORY_RESERVED_TOPIC = "inventory.reserved";
    public static final String INVENTORY_OUT_OF_STOCK_TOPIC = "inventory.out-of-stock";
    public static final String INVENTORY_RESTORED_TOPIC = "inventory.restored";
    public static final String PAYMENT_PAID_TOPIC = "payment.paid";
    public static final String PAYMENT_NOT_PAID_TOPIC = "payment.not-paid";
    public static final String SHIPPING_CREATED_TOPIC = "shipping.created";
}
