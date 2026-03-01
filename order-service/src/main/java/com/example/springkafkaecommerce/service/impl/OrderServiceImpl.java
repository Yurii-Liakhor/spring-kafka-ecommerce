package com.example.springkafkaecommerce.service.impl;

import com.example.springkafkaecommerce.dto.OrderDTO;
import com.example.springkafkaecommerce.dto.OrderItemDTO;
import com.example.springkafkaecommerce.entity.Order;
import com.example.springkafkaecommerce.entity.OrderItem;
import com.example.springkafkaecommerce.event.OrderEvent;
import com.example.springkafkaecommerce.event.ProductReservationItem;
import com.example.springkafkaecommerce.kafka.KafkaTopics;
import com.example.springkafkaecommerce.model.OrderStatus;
import com.example.springkafkaecommerce.repository.OrderRepository;
import com.example.springkafkaecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final Converter<Order, OrderDTO> orderConverter;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(
            KafkaTemplate<String, OrderEvent> kafkaTemplate,
            Converter<Order, OrderDTO> orderConverter,
            OrderRepository orderRepository
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderConverter = orderConverter;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public String createOrder(OrderDTO orderDTO) {
        String orderUuid = UUID.randomUUID().toString();
        log.debug("Creating orderUuid {}", orderUuid);

        Order order = buildOrder(orderUuid, orderDTO);
        orderRepository.save(order);

        OrderEvent orderEvent = createOrderEvent(orderUuid, orderDTO);
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED_TOPIC, orderUuid, orderEvent);

        return orderUuid;
    }

    private Order buildOrder(String orderUuid, OrderDTO orderDTO) {
        Order order = new Order();
        order.setUuid(orderUuid);
        order.setStatus(OrderStatus.PENDING);

        if (orderDTO.getOrderItems() != null) {
            for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                OrderItem item = new OrderItem();
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                order.addItem(item);
            }
        }
        return order;
    }

    private OrderEvent createOrderEvent(String orderUuid, OrderDTO orderDTO) {
        List<ProductReservationItem> products = orderDTO.getOrderItems() == null
                ? Collections.emptyList()
                : orderDTO.getOrderItems().stream()
                .map(this::toProductReservationEvent)
                .toList();
        return new OrderEvent(orderUuid, products);
    }

    private ProductReservationItem toProductReservationEvent(OrderItemDTO product) {
        return new ProductReservationItem(product.getProductId(), product.getQuantity());
    }

    @Transactional
    @Override
    public void cancelOrder(String orderUuid) {
        log.debug("Canceling order {}", orderUuid);

        updateStatus(orderUuid, OrderStatus.CANCELLED);
    }

    @Transactional
    @Override
    public void confirmOrder(String orderUuid) {
        log.debug("Confirming order {}", orderUuid);

        updateStatus(orderUuid, OrderStatus.CONFIRMED);
    }

    @Transactional
    @Override
    public void reserveOrder(String orderUuid) {
        log.debug("Reserving order {}", orderUuid);

        updateStatus(orderUuid, OrderStatus.INVENTORY_RESERVED);
    }

    private void updateStatus(String orderUuid, OrderStatus status) {
        Order order = orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderUuid));

        if (order.getStatus() == status) {
            return;
        }

        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    public OrderDTO getOrder(String orderUuid) {
        log.debug("Getting order {}", orderUuid);
        return orderRepository.findByUuid(orderUuid)
                .map(orderConverter::convert)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderUuid));
    }


    @Override
    public List<OrderDTO> getAllOrders() {
        log.debug("Getting all orders");
        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            orderDTOs.add(orderConverter.convert(order));
        }
        return orderDTOs;
    }
}
