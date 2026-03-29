package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.OutboxEvent;
import com.example.springkafkaecommerce.entity.Payment;
import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.event.PaymentEvent;
import com.example.springkafkaecommerce.exception.PaymentRejectedException;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.model.PaymentProvider;
import com.example.springkafkaecommerce.model.PaymentStatus;
import com.example.springkafkaecommerce.repository.OutboxRepository;
import com.example.springkafkaecommerce.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryEventHandler {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayProviderFactory paymentGatewayProviderFactory;

    public InventoryEventHandler(OutboxRepository outboxRepository,
                                 ObjectMapper objectMapper,
                                 PaymentRepository paymentRepository,
                                 PaymentGatewayProviderFactory paymentGatewayProviderFactory) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.paymentGatewayProviderFactory = paymentGatewayProviderFactory;
    }

    // @Transactional гарантує що збереження Payment і OutboxEvent — одна DB транзакція.
    // Якщо будь-яка операція провалиться — обидві відкотяться.
    @Transactional
    public void handlePayment(InventoryEvent event) {
        Optional<Payment> existing = paymentRepository.findByOrderUuid(event.orderUuid());
        if (existing.isPresent()) {
            republish(event, existing.get());
            return;
        }

        var paymentData = event.paymentData();
        String paymentUuid = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .orderUuid(event.orderUuid())
                .paymentUuid(paymentUuid)
                .status(PaymentStatus.PENDING)
                .amount(paymentData.amount())
                .currency(paymentData.currency())
                .paymentMethod(paymentData.paymentMethod())
                .provider(PaymentProvider.valueOf(paymentData.provider()))
                .createdAt(Instant.now())
                .build();
        payment = paymentRepository.save(payment);

        chargeAndSaveOutbox(payment, event);
    }

    private void republish(InventoryEvent event, Payment payment) {
        if (payment.getStatus() == PaymentStatus.PENDING) {
            chargeAndSaveOutbox(payment, event);
            return;
        }

        String topic = payment.getStatus() == PaymentStatus.SUCCESS
                ? KafkaTopics.PAYMENT_PAID_TOPIC
                : KafkaTopics.PAYMENT_NOT_PAID_TOPIC;

        if (outboxRepository.existsByAggregateIdAndTopic(payment.getPaymentUuid(), topic)) {
            return;
        }

        saveOutbox(
                payment.getPaymentUuid(),
                topic,
                new PaymentEvent(event.orderUuid(), event.reserveProducts(), event.paymentData())
        );
    }

    private void chargeAndSaveOutbox(Payment payment, InventoryEvent event) {
        String topic;
        try {
            paymentGatewayProviderFactory.charge(payment);
            payment.setStatus(PaymentStatus.SUCCESS);
            topic = KafkaTopics.PAYMENT_PAID_TOPIC;
        } catch (PaymentRejectedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            topic = KafkaTopics.PAYMENT_NOT_PAID_TOPIC;
        }
        paymentRepository.save(payment);
        saveOutbox(payment.getPaymentUuid(), topic, new PaymentEvent(event.orderUuid(), event.reserveProducts(), event.paymentData()));
    }

    private void saveOutbox(String aggregateId, String topic, PaymentEvent paymentEvent) {
        try {
            outboxRepository.save(OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .topic(topic)
                    .payload(objectMapper.writeValueAsString(paymentEvent))
                    .eventClass(PaymentEvent.class.getName())
                    .createdAt(Instant.now())
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize PaymentEvent for aggregateId: " + aggregateId, e);
        }
    }
}