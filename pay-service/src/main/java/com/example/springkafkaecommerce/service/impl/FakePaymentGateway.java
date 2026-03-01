package com.example.springkafkaecommerce.service.impl;

import com.example.springkafkaecommerce.entity.Payment;
import com.example.springkafkaecommerce.exception.PaymentRejectedException;
import com.example.springkafkaecommerce.service.PaymentGateway;
import org.springframework.stereotype.Service;

@Service
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public void charge(Payment payment) throws PaymentRejectedException {
        if (payment.getAmount() > 1000) {
            throw new PaymentRejectedException("Insufficient funds");
        }
    }
}
