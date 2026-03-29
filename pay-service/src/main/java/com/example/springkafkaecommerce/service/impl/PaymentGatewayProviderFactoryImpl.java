package com.example.springkafkaecommerce.service.impl;

import com.example.springkafkaecommerce.entity.Payment;
import com.example.springkafkaecommerce.service.PaymentGatewayProvider;
import com.example.springkafkaecommerce.service.PaymentGatewayProviderFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentGatewayProviderFactoryImpl implements PaymentGatewayProviderFactory {

    private final List<PaymentGatewayProvider> paymentGatewayProviders;

    public PaymentGatewayProviderFactoryImpl(List<PaymentGatewayProvider> paymentGatewayProviders) {
        this.paymentGatewayProviders = paymentGatewayProviders;
    }

    @Override
    public void charge(Payment payment) {
        getPaymentProvider(payment).charge(payment);
    }

    @Override
    public void refund(Payment payment) {
        getPaymentProvider(payment).refund(payment);
    }

    private PaymentGatewayProvider getPaymentProvider(Payment payment) {
        return paymentGatewayProviders.stream()
                .filter(provider -> provider.isPaymentSupported(payment.getPaymentMethod(), payment.getProvider()))
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = String.format("No payment provider found for payment method: %s and provider: %s",
                            payment.getPaymentMethod(), payment.getProvider());
                    return new IllegalArgumentException(errorMessage);
                });
    }
}
