package com.example.springkafkaecommerce.service.impl;

import com.example.springkafkaecommerce.entity.Payment;
import com.example.springkafkaecommerce.exception.PaymentRejectedException;
import com.example.springkafkaecommerce.model.PaymentProvider;
import com.example.springkafkaecommerce.service.PaymentGatewayProvider;
import org.springframework.stereotype.Service;

@Service
public class PaypalPaymentGatewayProviderStub implements PaymentGatewayProvider {

    @Override
    public void charge(Payment payment) throws PaymentRejectedException {
        if (payment.getAmount() > 1000) {
            throw new PaymentRejectedException("Insufficient funds");
        }
    }

    @Override
    public void refund(Payment payment) throws PaymentRejectedException {
        if (payment.getAmount() > 1000) {
            throw new PaymentRejectedException("Insufficient funds");
        }
    }

    @Override
    public boolean isPaymentSupported(String paymentMethod, PaymentProvider provider) {
        return provider == PaymentProvider.PAYPAL;
    }
}
