package com.example.springkafkaecommerce.dto;

import com.example.springkafkaecommerce.event.PaymentData;
import com.example.springkafkaecommerce.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String orderUuid;
    private OrderStatus status;
    private List<OrderItemDTO> orderItems;
    private PaymentData paymentData;
}
