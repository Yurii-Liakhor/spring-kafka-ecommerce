package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.entity.Payment;
import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.model.PaymentStatus;
import com.example.springkafkaecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class InventoryEventHandler {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ProcessedEventService processedEventService;

    public InventoryEventHandler(PaymentRepository paymentRepository,
                                 PaymentGateway paymentGateway,
                                 ProcessedEventService processedEventService) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.processedEventService = processedEventService;
    }

    @Transactional
    public void handlePayment(InventoryEvent event) {

        Payment payment = paymentRepository.save(Payment.builder()
                .orderUuid(event.orderUuid())
                .status(PaymentStatus.PENDING)
                .amount(100)
                .createdAt(Instant.now())
                .build()
        );

        paymentGateway.charge(payment);
        processedEventService.markAsProcessed(event.orderUuid(), KafkaTopics.INVENTORY_RESERVED_TOPIC);
    }

}