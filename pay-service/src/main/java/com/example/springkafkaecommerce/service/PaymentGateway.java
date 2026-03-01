package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.Payment;
import com.example.springkafkaecommerce.exception.PaymentRejectedException;

public interface PaymentGateway {
    void charge(Payment payment) throws PaymentRejectedException;
}
