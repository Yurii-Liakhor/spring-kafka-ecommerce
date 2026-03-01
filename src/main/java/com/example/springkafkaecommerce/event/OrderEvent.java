package com.example.springkafkaecommerce.event;

import java.util.List;

public record OrderEvent(
        String orderUuid,
        List<ProductReservationItem> reserveProducts
) {}
