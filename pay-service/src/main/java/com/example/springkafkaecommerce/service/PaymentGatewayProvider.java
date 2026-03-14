package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.Payment;
import com.example.springkafkaecommerce.exception.PaymentRejectedException;
import com.example.springkafkaecommerce.model.PaymentProvider;

public interface PaymentGatewayProvider {
    void charge(Payment payment) throws PaymentRejectedException;

    void refund(Payment payment) throws PaymentRejectedException;

    boolean isPaymentSupported(String paymentMethod, PaymentProvider provider);
}
