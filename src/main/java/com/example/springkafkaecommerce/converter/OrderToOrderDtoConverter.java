package com.example.springkafkaecommerce.converter;

import com.example.springkafkaecommerce.dto.OrderDTO;
import com.example.springkafkaecommerce.dto.OrderItemDTO;
import com.example.springkafkaecommerce.entity.Order;
import com.example.springkafkaecommerce.entity.OrderItem;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderToOrderDtoConverter implements Converter<Order, OrderDTO> {

    @Override
    public OrderDTO convert(Order source) {
        if (source == null) {
            return null;
        }

        List<OrderItemDTO> items = source.getItems() == null
                ? Collections.emptyList()
                : source.getItems().stream()
                .map(this::toOrderItemDto)
                .toList();

        return OrderDTO.builder()
                .orderItems(items)
                .build();
    }

    private OrderItemDTO toOrderItemDto(OrderItem item) {
        Long productId = item.getProduct() == null ? null : item.getProduct().getId();

        return OrderItemDTO.builder()
                .productId(productId)
                .quantity(item.getQuantity())
                .build();
    }
}