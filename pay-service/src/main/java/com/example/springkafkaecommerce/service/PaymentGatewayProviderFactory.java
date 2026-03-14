package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.Payment;

public interface PaymentGatewayProviderFactory {
    void charge(Payment payment);

    void refund(Payment payment);
}
