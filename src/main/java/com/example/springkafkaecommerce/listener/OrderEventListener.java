package com.example.springkafkaecommerce.listener;

import com.example.springkafkaecommerce.entity.Order;
import com.example.springkafkaecommerce.entity.ProcessedEvent;
import com.example.springkafkaecommerce.event.InventoryEvent;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.model.OrderStatus;
import com.example.springkafkaecommerce.repository.OrderRepository;
import com.example.springkafkaecommerce.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.NoSuchElementException;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private static final String ORDER_SERVICE_GROUP = "order-service-group";

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    public OrderEventListener(OrderRepository orderRepository,
                              ProcessedEventRepository processedEventRepository) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED_TOPIC, groupId = ORDER_SERVICE_GROUP)
    public void consumeProductReserved(InventoryEvent inventoryEvent) {
        String orderUuid = inventoryEvent.orderUuid();
        log.debug("Consuming inventory reserved event {}", orderUuid);

        if (!markAsProcessed(inventoryEvent.orderUuid(), KafkaTopics.INVENTORY_RESERVED_TOPIC)) {
            log.warn("Duplicate event skipped orderUuid: {}", inventoryEvent.orderUuid());
            return;
        }

        Order order = orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderUuid));
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);
    }

    private boolean markAsProcessed(String eventId, String topic) {
        try {
            processedEventRepository.saveAndFlush(ProcessedEvent.builder()
                    .eventId(eventId)
                    .topic(topic)
                    .processedAt(Instant.now())
                    .build());
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
